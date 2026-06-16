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

    private static final long ROLL_COOLDOWN_MS = 60 * 60 * 1000L; // 1 hora → 16 rolls
    private static final long CLAIM_COOLDOWN_MS = 2 * 60 * 60 * 1000L; // 2 horas

    private long lastRollTime = 0;
    private long lastClaimTime = 0;
    private int rollsRemaining = 16;

    // stat indices: 0=vida, 1=velocidad, 2=fuerza, 3=defensa, 4=vel.minería
    private int[] statLevels = new int[]{0, 0, 0, 0, 0};

    public static final int[] STAT_COSTS  = {400, 500, 700, 450, 300};
    public static final int   STAT_MAX    = 5;
    public static final String[] STAT_NAMES = {"Vida", "Velocidad", "Fuerza", "Defensa", "Vel. Minería"};

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() { return playerId; }
    public List<Character> getHarem() { return harem; }
    public int getKakera() { return kakera; }
    public int[] getStatLevels() { return statLevels; }

    public void addKakera(int amount) { this.kakera += amount; }
    public boolean spendKakera(int amount) {
        if (kakera < amount) return false;
        kakera -= amount;
        return true;
    }

    public boolean canRoll() {
        long now = System.currentTimeMillis();
        if (rollsRemaining > 0) return true;
        if (now - lastRollTime >= ROLL_COOLDOWN_MS) {
            rollsRemaining = 16;
            return true;
        }
        return false;
    }

    public void useRoll() {
        if (rollsRemaining == 16) lastRollTime = System.currentTimeMillis();
        rollsRemaining = Math.max(0, rollsRemaining - 1);
    }

    public long getRollCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastRollTime;
        return Math.max(0, ROLL_COOLDOWN_MS - elapsed);
    }

    public int getRollsRemaining() {
        if (rollsRemaining == 0 && System.currentTimeMillis() - lastRollTime >= ROLL_COOLDOWN_MS) {
            rollsRemaining = 16;
        }
        return rollsRemaining;
    }

    public boolean canClaim() {
        return System.currentTimeMillis() - lastClaimTime >= CLAIM_COOLDOWN_MS;
    }

    public long getClaimCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastClaimTime;
        return Math.max(0, CLAIM_COOLDOWN_MS - elapsed);
    }

    public void claim(Character character) {
        harem.add(character);
        lastClaimTime = System.currentTimeMillis();
    }

    public boolean removeFromHarem(int characterId) {
        return harem.removeIf(c -> c.id() == characterId);
    }

    public boolean hasCharacter(int characterId) {
        return harem.stream().anyMatch(c -> c.id() == characterId);
    }

    public void resetCooldowns() {
        this.rollsRemaining = 16;
        this.lastRollTime = 0;
        this.lastClaimTime = 0;
    }

    // Cada nivel cuesta un 50% más que el anterior
    public static int getStatCost(int statIndex, int currentLevel) {
        return (int)(STAT_COSTS[statIndex] * Math.pow(1.5, currentLevel));
    }

    public boolean upgradeStat(int statIndex) {
        if (statIndex < 0 || statIndex >= 5) return false;
        if (statLevels[statIndex] >= STAT_MAX) return false;
        int cost = getStatCost(statIndex, statLevels[statIndex]);
        if (!spendKakera(cost)) return false;
        statLevels[statIndex]++;
        return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerId", playerId);
        tag.putInt("kakera", kakera);
        tag.putLong("lastRollTime", lastRollTime);
        tag.putLong("lastClaimTime", lastClaimTime);
        tag.putInt("rollsRemaining", rollsRemaining);
        tag.putIntArray("statLevels", statLevels);

        ListTag haremTag = new ListTag();
        for (Character c : harem) {
            CompoundTag ct = new CompoundTag();
            ct.putInt("id", c.id());
            ct.putString("name", c.name());
            ct.putString("animeName", c.animeName());
            ct.putString("skinUUID", c.imageUrl());
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
        if (tag.contains("statLevels")) {
            int[] loaded = tag.getIntArray("statLevels");
            if (loaded.length >= 4) {
                // migrate from 4-stat saves to 5-stat
                int[] s = new int[5];
                System.arraycopy(loaded, 0, s, 0, Math.min(loaded.length, 5));
                data.statLevels = s;
            }
        }

        ListTag haremTag = tag.getList("harem", Tag.TAG_COMPOUND);
        for (int i = 0; i < haremTag.size(); i++) {
            CompoundTag ct = haremTag.getCompound(i);
            data.harem.add(new Character(
                ct.getInt("id"),
                ct.getString("name"),
                ct.getString("animeName"),
                ct.getString("skinUUID"),
                ct.getInt("kakeraValue")
            ));
        }
        return data;
    }
}
