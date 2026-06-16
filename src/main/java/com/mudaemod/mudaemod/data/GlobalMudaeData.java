package com.mudaemod.mudaemod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlobalMudaeData extends SavedData {

    private static final String NAME = "mudae_global";
    private static final long CLAIM_WINDOW_MS = 3 * 60 * 1000L;

    private final Set<Integer> claimedIds = new HashSet<>();

    // Active roll
    private int activeCharId = -1;
    private String activeCharName = "";
    private String activeCharAnime = "";
    private int activeKakera = 0;
    private long activeRolledAt = 0;
    private UUID activeRolledBy = null;

    public static GlobalMudaeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(GlobalMudaeData::new, GlobalMudaeData::load, null),
            NAME
        );
    }

    public Set<Integer> getClaimedIds() { return claimedIds; }

    public void claimCharacter(int id) {
        claimedIds.add(id);
        setDirty();
    }

    public void unclaimCharacter(int id) {
        claimedIds.remove(id);
        setDirty();
    }

    public void setActiveRoll(Character character, UUID rolledBy) {
        this.activeCharId = character.id();
        this.activeCharName = character.name();
        this.activeCharAnime = character.animeName();
        this.activeKakera = character.kakeraValue();
        this.activeRolledAt = System.currentTimeMillis();
        this.activeRolledBy = rolledBy;
        setDirty();
    }

    public void clearActiveRoll() {
        this.activeCharId = -1;
        this.activeRolledAt = 0;
        this.activeRolledBy = null;
        setDirty();
    }

    public boolean hasActiveRoll() {
        return activeCharId != -1 && !isExpired();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - activeRolledAt > CLAIM_WINDOW_MS;
    }

    public int getActiveCharId() { return activeCharId; }
    public String getActiveCharName() { return activeCharName; }
    public String getActiveCharAnime() { return activeCharAnime; }
    public int getActiveKakera() { return activeKakera; }
    public UUID getActiveRolledBy() { return activeRolledBy; }

    @Override
    public CompoundTag save(CompoundTag tag) {
        int[] ids = claimedIds.stream().mapToInt(Integer::intValue).toArray();
        tag.put("claimedIds", new IntArrayTag(ids));
        tag.putInt("activeCharId", activeCharId);
        tag.putString("activeCharName", activeCharName);
        tag.putString("activeCharAnime", activeCharAnime);
        tag.putInt("activeKakera", activeKakera);
        tag.putLong("activeRolledAt", activeRolledAt);
        if (activeRolledBy != null) tag.putUUID("activeRolledBy", activeRolledBy);
        return tag;
    }

    public static GlobalMudaeData load(CompoundTag tag) {
        GlobalMudaeData data = new GlobalMudaeData();
        for (int id : tag.getIntArray("claimedIds")) data.claimedIds.add(id);
        data.activeCharId = tag.getInt("activeCharId");
        data.activeCharName = tag.getString("activeCharName");
        data.activeCharAnime = tag.getString("activeCharAnime");
        data.activeKakera = tag.getInt("activeKakera");
        data.activeRolledAt = tag.getLong("activeRolledAt");
        if (tag.contains("activeRolledBy")) data.activeRolledBy = tag.getUUID("activeRolledBy");
        return data;
    }
}
