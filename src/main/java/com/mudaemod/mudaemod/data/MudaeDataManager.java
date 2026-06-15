package com.mudaemod.mudaemod.data;

import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MudaeDataManager {

    private static MudaeDataManager instance;
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private File saveDir;

    private MudaeDataManager() {}

    public static MudaeDataManager get() {
        if (instance == null) instance = new MudaeDataManager();
        return instance;
    }

    public void init(MinecraftServer server) {
        saveDir = new File(server.getServerDirectory().toFile(), "mudae_data");
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
        if (data == null) return;
        File file = new File(saveDir, id + ".nbt");
        try {
            NbtIo.write(data.save(), file.toPath());
        } catch (IOException e) {
            MudaeMod.LOGGER.error("Error saving player data for {}", id, e);
        }
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
