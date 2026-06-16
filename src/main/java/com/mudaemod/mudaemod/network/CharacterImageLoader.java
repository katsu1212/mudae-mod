package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class CharacterImageLoader {

    public record ImageInfo(ResourceLocation location, int width, int height) {}

    private static final Map<Integer, ImageInfo> CACHE = new ConcurrentHashMap<>();

    public static void load(int charId, String imageUrl, Consumer<ImageInfo> onLoaded) {
        ImageInfo cached = CACHE.get(charId);
        if (cached != null) {
            onLoaded.accept(cached);
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                MudaeMod.LOGGER.info("[Mudae] Descargando imagen para char {} desde {}", charId, imageUrl);

                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) MudaeMod/1.3");
                conn.setRequestProperty("Accept", "image/jpeg,image/png,image/*,*/*");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setInstanceFollowRedirects(true);
                conn.connect();

                int code = conn.getResponseCode();
                MudaeMod.LOGGER.info("[Mudae] HTTP {} para char {}", code, charId);

                if (code != 200) return null;

                try (InputStream is = conn.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    MudaeMod.LOGGER.info("[Mudae] Descargados {} bytes para char {}", bytes.length, charId);
                    return bytes;
                }
            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] Error descargando imagen para char {}: {}", charId, e.getMessage());
                return null;
            }
        }).thenAccept(bytes -> {
            if (bytes == null || bytes.length == 0) {
                MudaeMod.LOGGER.warn("[Mudae] Sin bytes para char {}", charId);
                return;
            }

            // NativeImage.read() puede correr off-thread (solo usa memoria)
            NativeImage img;
            try {
                img = NativeImage.read(new ByteArrayInputStream(bytes));
                MudaeMod.LOGGER.info("[Mudae] NativeImage creado para char {}: {}x{}", charId, img.getWidth(), img.getHeight());
            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] NativeImage.read falló para char {}: {}", charId, e.getMessage());
                return;
            }

            final NativeImage finalImg = img;

            // Upload a GPU debe ser en el render thread
            Minecraft.getInstance().execute(() -> {
                try {
                    ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "char/" + charId);

                    DynamicTexture texture = new DynamicTexture(finalImg);

                    // register() solo guarda en el mapa — upload() sube los pixels a la GPU
                    Minecraft.getInstance().getTextureManager().register(loc, texture);
                    texture.upload();

                    int w = finalImg.getWidth();
                    int h = finalImg.getHeight();
                    ImageInfo info = new ImageInfo(loc, w, h);
                    CACHE.put(charId, info);

                    MudaeMod.LOGGER.info("[Mudae] Textura subida a GPU para char {} ({}x{})", charId, w, h);
                    onLoaded.accept(info);
                } catch (Exception e) {
                    MudaeMod.LOGGER.error("[Mudae] Error subiendo textura para char {}: {}", charId, e.getMessage());
                }
            });
        });
    }
}
