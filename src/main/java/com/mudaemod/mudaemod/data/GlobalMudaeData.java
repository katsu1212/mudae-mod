package com.mudaemod.mudaemod.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GlobalMudaeData extends SavedData {

    private static final String NAME = "mudae_global";
    public static final long CLAIM_WINDOW_MS = 30_000L;

    private final Set<Integer> claimedIds = new HashSet<>();

    // In-memory only — rolls expire in 30s, no need to persist
    private final Map<Integer, PendingRoll> pendingRolls = new LinkedHashMap<>();

    public record PendingRoll(int id, String name, String anime, int kakera, long rolledAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() - rolledAt > CLAIM_WINDOW_MS;
        }
        public long secondsLeft() {
            return Math.max(0, (CLAIM_WINDOW_MS - (System.currentTimeMillis() - rolledAt)) / 1000);
        }
    }

    public static GlobalMudaeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(GlobalMudaeData::new,
                (tag, provider) -> load(tag), null),
            NAME
        );
    }

    public Set<Integer> getClaimedIds() { return claimedIds; }
    public void claimCharacter(int id)   { claimedIds.add(id);    setDirty(); }
    public void unclaimCharacter(int id) { claimedIds.remove(id); setDirty(); }

    /** Agrega un personaje al pool de claims pendientes (máx 30s). */
    public void addPendingRoll(Character c) {
        purgeExpired();
        pendingRolls.put(c.id(), new PendingRoll(c.id(), c.name(), c.animeName(), c.kakeraValue(),
            System.currentTimeMillis()));
    }

    /** Devuelve el roll pendiente si aún no expiró, null si no existe o expiró. */
    public PendingRoll getPendingRoll(int charId) {
        PendingRoll r = pendingRolls.get(charId);
        if (r == null) return null;
        if (r.isExpired()) { pendingRolls.remove(charId); return null; }
        return r;
    }

    public boolean hasPendingRoll(int charId) { return getPendingRoll(charId) != null; }

    public void removePendingRoll(int charId) { pendingRolls.remove(charId); }

    private void purgeExpired() {
        pendingRolls.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        int[] ids = claimedIds.stream().mapToInt(Integer::intValue).toArray();
        tag.put("claimedIds", new IntArrayTag(ids));
        return tag;
    }

    public static GlobalMudaeData load(CompoundTag tag) {
        GlobalMudaeData data = new GlobalMudaeData();
        for (int id : tag.getIntArray("claimedIds")) data.claimedIds.add(id);
        return data;
    }
}
