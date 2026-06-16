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

    private static final Map<Integer, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public static void load(int charId, String imageUrl, Consumer<ResourceLocation> onLoaded) {
        ResourceLocation cached = CACHE.get(charId);
        if (cached != null) {
            onLoaded.accept(cached);
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
                HttpResponse<byte[]> res = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
                if (res.statusCode() == 200) return res.body();
            } catch (Exception e) {
                MudaeMod.LOGGER.warn("Failed to download character image {}: {}", charId, e.getMessage());
            }
            return null;
        }).thenAccept(bytes -> {
            if (bytes == null) return;
            // Schedule texture creation on the main/render thread
            Minecraft.getInstance().execute(() -> {
                try {
                    NativeImage img = NativeImage.read(bytes);
                    DynamicTexture texture = new DynamicTexture(img);
                    ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "char/" + charId);
                    Minecraft.getInstance().getTextureManager().register(loc, texture);
                    CACHE.put(charId, loc);
                    onLoaded.accept(loc);
                } catch (Exception e) {
                    MudaeMod.LOGGER.warn("Failed to create texture for char {}: {}", charId, e.getMessage());
                }
            });
        });
    }
}
