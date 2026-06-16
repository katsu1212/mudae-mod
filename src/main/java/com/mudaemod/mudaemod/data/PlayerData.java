package com.mudaemod.mudaemod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID playerId;
    private final List<Character> harem = new ArrayList<>();
    private int kakera = 0;

    // Cooldown en milisegundos (1 hora como Mudae)
    private static final long ROLL_COOLDOWN_MS = 60 * 60 * 1000L;
    private static final long CLAIM_COOLDOWN_MS = 3 * 60 * 60 * 1000L;

    private long lastRollTime = 0;
    private long lastClaimTime = 0;
    private int rollsRemaining = 10;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() { return playerId; }
    public List<Character> getHarem() { return harem; }
    public int getKakera() { return kakera; }

    public void addKakera(int amount) { this.kakera += amount; }

    public boolean canRoll() {
        long now = System.currentTimeMillis();
        if (rollsRemaining > 0) return true;
        if (now - lastRollTime >= ROLL_COOLDOWN_MS) {
            rollsRemaining = 10;
            lastRollTime = now;
            return true;
        }
        return false;
    }

    public void useRoll() {
        rollsRemaining--;
        if (rollsRemaining == 0) lastRollTime = System.currentTimeMillis();
    }

    public long getRollCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastRollTime;
        return Math.max(0, ROLL_COOLDOWN_MS - elapsed);
    }

    public boolean canClaim() {
        return System.currentTimeMillis() - lastClaimTime >= CLAIM_COOLDOWN_MS;
    }

    public void claim(Character character) {
        harem.add(character);
        lastClaimTime = System.currentTimeMillis();
        kakera += character.kakeraValue();
    }

    public boolean hasCharacter(int characterId) {
        return harem.stream().anyMatch(c -> c.id() == characterId);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerId", playerId);
        tag.putInt("kakera", kakera);
        tag.putLong("lastRollTime", lastRollTime);
        tag.putLong("lastClaimTime", lastClaimTime);
        tag.putInt("rollsRemaining", rollsRemaining);

        ListTag haremTag = new ListTag();
        for (Character c : harem) {
            CompoundTag ct = new CompoundTag();
            ct.putInt("id", c.id());
            ct.putString("name", c.name());
            ct.putString("animeName", c.animeName());
            ct.putString("imageUrl", c.imageUrl());
            ct.putInt("kakeraValue", c.kakeraValue());
            haremTag.add(ct);
        }
        tag.put("harem", haremTag);
        return tag;
    }

    public static PlayerData load(CompoundTag tag) {
        PlayerData data = new PlayerData(tag.getUUID("playerId"));
        data.kakera = tag.getInt("kakera");
        data.lastRollTime = tag.getLong("lastRollTime");
        data.lastClaimTime = tag.getLong("lastClaimTime");
        data.rollsRemaining = tag.getInt("rollsRemaining");

        ListTag haremTag = tag.getList("harem", Tag.TAG_COMPOUND);
        for (int i = 0; i < haremTag.size(); i++) {
            CompoundTag ct = haremTag.getCompound(i);
            data.harem.add(new Character(
                ct.getInt("id"),
                ct.getString("name"),
                ct.getString("animeName"),
                ct.getString("imageUrl"),
                ct.getInt("kakeraValue")
            ));
        }
        return data;
    }
}
