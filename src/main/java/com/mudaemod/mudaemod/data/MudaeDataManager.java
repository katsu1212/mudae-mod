package com.mudaemod.mudaemod.data;

import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MudaeDataManager {

    private static MudaeDataManager instance;
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Map<UUID, ActiveRoll> activeRolls = new ConcurrentHashMap<>();
    private File saveDir;

    private MudaeDataManager() {}

    public static MudaeDataManager get() {
        if (instance == null) instance = new MudaeDataManager();
        return instance;
    }

    public void init(MinecraftServer server) {
        // Guardar dentro de la carpeta del mundo para que sea por-mundo
        saveDir = server.getWorldPath(LevelResource.ROOT).resolve("mudae_data").toFile();
        saveDir.mkdirs();
    }

    public PlayerData getPlayer(UUID id) {
        return players.computeIfAbsent(id, uuid -> {
            PlayerData data = loadFromDisk(uuid);
            return data != null ? data : new PlayerData(uuid);
        });
    }

    public void savePlayer(UUID id) {
        PlayerData data = players.get(id);
        if (data == null || saveDir == null) return;
        File file = new File(saveDir, id + ".nbt");
        try {
            NbtIo.write(data.save(), file.toPath());
        } catch (IOException e) {
            MudaeMod.LOGGER.error("Error saving player data for {}", id, e);
        }
    }

    public void setActiveRoll(UUID key, ActiveRoll roll) {
        activeRolls.put(key, roll);
    }

    public ActiveRoll getActiveRoll(UUID key) {
        return activeRolls.get(key);
    }

    public void removeActiveRoll(UUID key) {
        activeRolls.remove(key);
    }

    /** Busca en jugadores cargados y luego en disco quién tiene el personaje. */
    public UUID findOwnerOf(int charId) {
        for (Map.Entry<UUID, PlayerData> e : players.entrySet())
            if (e.getValue().hasCharacter(charId)) return e.getKey();
        if (saveDir == null) return null;
        File[] files = saveDir.listFiles((d, n) -> n.endsWith(".nbt"));
        if (files == null) return null;
        for (File f : files) {
            try {
                UUID uuid = UUID.fromString(f.getName().replace(".nbt", ""));
                if (players.containsKey(uuid)) continue;
                CompoundTag tag = NbtIo.read(f.toPath());
                if (tag != null && PlayerData.load(tag).hasCharacter(charId)) return uuid;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private PlayerData loadFromDisk(UUID id) {
        if (saveDir == null) return null;
        File file = new File(saveDir, id + ".nbt");
        if (!file.exists()) return null;
        try {
            CompoundTag tag = NbtIo.read(file.toPath());
            return tag != null ? PlayerData.load(tag) : null;
        } catch (IOException e) {
            MudaeMod.LOGGER.error("Error loading player data for {}", id, e);
            return null;
        }
    }
}
