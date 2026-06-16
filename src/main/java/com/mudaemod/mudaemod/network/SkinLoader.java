package com.mudaemod.mudaemod.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class SkinLoader {

    /**
     * Creates a fake player for rendering.
     * If the Entry has a texture URL (starts with "http"), injects it directly
     * into the GameProfile so Minecraft loads it without a UUID lookup.
     * Otherwise falls back to UUID-based skin loading.
     */
    public static CompletableFuture<RemotePlayer> createPlayer(String skinUUID, String characterName, int characterId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return null;

                GameProfile profile;

                if (skinUUID.startsWith("http")) {
                    // Direct texture URL — inject into GameProfile textures property
                    profile = new GameProfile(UUID.randomUUID(), characterName);
                    String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUUID + "\"}}}";
                    String encoded = Base64.getEncoder().encodeToString(json.getBytes());
                    profile.getProperties().put("textures", new Property("textures", encoded));
                    MudaeMod.LOGGER.info("[Mudae] Skin via URL para '{}'", characterName);
                } else {
                    // UUID — let Mojang's SkinManager fetch it
                    UUID uuid = UUID.fromString(skinUUID);
                    profile = new GameProfile(uuid, characterName);
                    MudaeMod.LOGGER.info("[Mudae] Skin via UUID para '{}' ({})", characterName, skinUUID);
                }

                RemotePlayer player = new RemotePlayer(mc.level, profile);
                mc.getSkinManager().getOrLoad(profile);
                return player;

            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] Error creando jugador para '{}': {}", characterName, e.getMessage());
                return null;
            }
        });
    }
}
