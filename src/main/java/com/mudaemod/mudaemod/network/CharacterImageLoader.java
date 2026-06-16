package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) MudaeMod/1.4");
                conn.setRequestProperty("Accept", "image/jpeg,image/png,image/*,*/*");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setInstanceFollowRedirects(true);
                conn.connect();

                int code = conn.getResponseCode();
                MudaeMod.LOGGER.info("[Mudae] HTTP {} para char {}", code, charId);
                if (code != 200) return null;

                byte[] rawBytes;
                try (InputStream is = conn.getInputStream()) {
                    rawBytes = is.readAllBytes();
                }
                MudaeMod.LOGGER.info("[Mudae] Descargados {} bytes para char {}", rawBytes.length, charId);
                if (rawBytes.length == 0) return null;

                // Use ImageIO to decode (handles JPEG, PNG, etc.) and re-encode as PNG
                // so NativeImage can always read it reliably
                BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(rawBytes));
                if (buffered == null) {
                    MudaeMod.LOGGER.error("[Mudae] ImageIO no pudo decodificar imagen para char {}", charId);
                    return null;
                }

                // Convert to ARGB to ensure alpha channel exists
                BufferedImage argb = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_ARGB);
                argb.createGraphics().drawImage(buffered, 0, 0, null);

                ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
                ImageIO.write(argb, "PNG", pngOut);
                byte[] pngBytes = pngOut.toByteArray();
                MudaeMod.LOGGER.info("[Mudae] PNG generado: {} bytes para char {} ({}x{})",
                    pngBytes.length, charId, argb.getWidth(), argb.getHeight());
                return pngBytes;

            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] Error descargando imagen para char {}: {}", charId, e.getMessage());
                return null;
            }
        }).thenAccept(pngBytes -> {
            if (pngBytes == null || pngBytes.length == 0) {
                MudaeMod.LOGGER.warn("[Mudae] Sin bytes PNG para char {}", charId);
                return;
            }

            NativeImage img;
            try {
                img = NativeImage.read(new ByteArrayInputStream(pngBytes));
                MudaeMod.LOGGER.info("[Mudae] NativeImage creado para char {}: {}x{}", charId, img.getWidth(), img.getHeight());
            } catch (Exception e) {
                MudaeMod.LOGGER.error("[Mudae] NativeImage.read falló para char {}: {}", charId, e.getMessage());
                return;
            }

            final NativeImage finalImg = img;

            Minecraft.getInstance().execute(() -> {
                try {
                    ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "char/" + charId);
                    DynamicTexture texture = new DynamicTexture(finalImg);
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
