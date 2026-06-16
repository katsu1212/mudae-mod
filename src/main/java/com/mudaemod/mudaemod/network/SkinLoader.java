package com.mudaemod.mudaemod.network;

import com.mojang.authlib.GameProfile;
import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class SkinLoader {

    /**
     * Creates a RemotePlayer using a pre-looked-up Mojang UUID.
     * Minecraft's SkinManager will automatically fetch and cache the skin texture.
     * The player model shows Steve/Alex immediately and switches to the real skin once loaded.
     */
    public static CompletableFuture<RemotePlayer> createPlayer(String skinUUID, String characterName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID uuid = UUID.fromString(skinUUID);
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return null;

                GameProfile profile = new GameProfile(uuid, characterName);
                RemotePlayer player = new RemotePlayer(mc.level, profile);

                // Trigger skin load via Minecraft's built-in SkinManager
                mc.getSkinManager().getOrLoad(profile);

                MudaeMod.LOGGER.info("[Mudae] Skin cargando para '{}' ({})", characterName, skinUUID);
                return player;
            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] Error creando RemotePlayer para '{}': {}", characterName, e.getMessage());
                return null;
            }
        });
    }
}
