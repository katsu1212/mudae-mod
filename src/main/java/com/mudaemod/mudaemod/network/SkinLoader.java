package com.mudaemod.mudaemod.network;

import com.mojang.authlib.GameProfile;
import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class SkinLoader {

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private static final Map<String, UUID> UUID_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a fake RemotePlayer for the given character name.
     * Looks up a Minecraft UUID by the character's first name via Mojang API,
     * then uses Minecraft's built-in SkinManager to load the skin.
     * The player model will initially show Steve/Alex and switch to the real skin once loaded.
     */
    public static CompletableFuture<RemotePlayer> createPlayerForCharacter(String characterName) {
        String firstName = characterName.split("[ _]")[0];

        return lookupUUID(firstName).thenApply(uuid -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return null;

                GameProfile profile = new GameProfile(uuid, firstName);
                RemotePlayer player = new RemotePlayer(mc.level, profile);

                // Trigger skin load via Minecraft's proven SkinManager (same system used for multiplayer skins)
                mc.getSkinManager().getOrLoad(profile);

                MudaeMod.LOGGER.info("[Mudae] RemotePlayer creado para '{}' con UUID {}", firstName, uuid);
                return player;
            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] Error creando RemotePlayer para '{}': {}", firstName, e.getMessage());
                return null;
            }
        });
    }

    private static CompletableFuture<UUID> lookupUUID(String name) {
        UUID cached = UUID_CACHE.get(name.toLowerCase());
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

                HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() == 200) {
                    String json = resp.body();
                    int idStart = json.indexOf("\"id\":\"") + 6;
                    int idEnd = json.indexOf("\"", idStart);
                    if (idStart > 5 && idEnd > idStart) {
                        String raw = json.substring(idStart, idEnd);
                        // Mojang returns UUID without dashes; reformat 8-4-4-4-12
                        String dashed = raw.substring(0, 8) + "-" + raw.substring(8, 12) + "-"
                            + raw.substring(12, 16) + "-" + raw.substring(16, 20) + "-" + raw.substring(20);
                        UUID uuid = UUID.fromString(dashed);
                        UUID_CACHE.put(name.toLowerCase(), uuid);
                        MudaeMod.LOGGER.info("[Mudae] UUID Mojang para '{}': {}", name, uuid);
                        return uuid;
                    }
                }
            } catch (Exception e) {
                MudaeMod.LOGGER.warn("[Mudae] Sin UUID Mojang para '{}': {}", name, e.getMessage());
            }
            // Offline fallback: deterministic UUID from name (always the same for the same character)
            UUID fallback = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
            UUID_CACHE.put(name.toLowerCase(), fallback);
            MudaeMod.LOGGER.info("[Mudae] UUID offline para '{}': {}", name, fallback);
            return fallback;
        });
    }
}
