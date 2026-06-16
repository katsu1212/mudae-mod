package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class CharacterImageLoader {

    public record ImageInfo(ResourceLocation location, int width, int height) {}

    private static final Map<Integer, ImageInfo> CACHE = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
        .build();

    public static void load(int charId, String imageUrl, Consumer<ImageInfo> onLoaded) {
        ImageInfo cached = CACHE.get(charId);
        if (cached != null) {
            onLoaded.accept(cached);
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                // AniList a veces redirige a CDN — seguimos redirects con ALWAYS
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "MudaeMod/1.2 Minecraft-Mod")
                    .header("Accept", "image/jpeg,image/png,image/*")
                    .GET()
                    .build();

                HttpResponse<byte[]> res = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
                MudaeMod.LOGGER.info("Image fetch for char {}: HTTP {} ({} bytes)", charId, res.statusCode(), res.body().length);

                if (res.statusCode() == 200 && res.body().length > 0) {
                    return res.body();
                }
            } catch (Exception e) {
                MudaeMod.LOGGER.warn("Failed to download image for char {}: {}", charId, e.getMessage());
            }
            return null;
        }).thenAccept(bytes -> {
            if (bytes == null) return;
            Minecraft.getInstance().execute(() -> {
                try {
                    NativeImage img = NativeImage.read(bytes);
                    int w = img.getWidth();
                    int h = img.getHeight();

                    DynamicTexture texture = new DynamicTexture(img);
                    ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "char/" + charId);
                    Minecraft.getInstance().getTextureManager().register(loc, texture);

                    ImageInfo info = new ImageInfo(loc, w, h);
                    CACHE.put(charId, info);
                    onLoaded.accept(info);

                    MudaeMod.LOGGER.info("Texture registered for char {} ({}x{})", charId, w, h);
                } catch (Exception e) {
                    MudaeMod.LOGGER.warn("Failed to create texture for char {}: {}", charId, e.getMessage());
                }
            });
        });
    }
}
