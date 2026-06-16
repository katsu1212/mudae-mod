package com.mudaemod.mudaemod.network;

import com.mojang.authlib.GameProfile;
import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class SkinLoader {

    /**
     * Creates a fake player to render in the GUI.
     * Priority: bundled skin PNG in mod assets → Mojang UUID skin → default
     */
    public static CompletableFuture<RemotePlayer> createPlayer(String skinUUID, String characterName, int characterId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return null;

                // Check for a bundled skin shipped inside the mod jar
                ResourceLocation bundled = ResourceLocation.fromNamespaceAndPath(
                    "mudaemod", "textures/skins/" + characterId + ".png");

                if (mc.getResourceManager().getResource(bundled).isPresent()) {
                    GameProfile profile = new GameProfile(UUID.randomUUID(), characterName);
                    MudaeMod.LOGGER.info("[Mudae] Usando skin empaquetada para '{}' (id={})", characterName, characterId);
                    return new MudaeFakePlayer(mc.level, profile, bundled);
                }

                // Fallback: use Mojang's skin manager with the stored UUID
                UUID uuid = UUID.fromString(skinUUID);
                GameProfile profile = new GameProfile(uuid, characterName);
                RemotePlayer player = new RemotePlayer(mc.level, profile);
                mc.getSkinManager().getOrLoad(profile);
                MudaeMod.LOGGER.info("[Mudae] Skin via Mojang para '{}' ({})", characterName, skinUUID);
                return player;

            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] Error creando jugador para '{}': {}", characterName, e.getMessage());
                return null;
            }
        });
    }
}
