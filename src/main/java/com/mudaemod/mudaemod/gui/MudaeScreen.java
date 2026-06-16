package com.mudaemod.mudaemod.gui;

import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.network.CharacterImageLoader;
import com.mudaemod.mudaemod.network.CharacterImageLoader.ImageInfo;
import com.mudaemod.mudaemod.network.CharacterResultPayload;
import com.mudaemod.mudaemod.network.ClaimRequestPayload;
import com.mudaemod.mudaemod.network.RollRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class MudaeScreen extends AbstractContainerScreen<MudaeMenu> {

    private enum State { IDLE, ROLLING, LOADING_IMAGE, DONE, ERROR }

    private static final int W = 280;
    private static final int H = 220;
    private static final int MAX_IMG_W = 120;
    private static final int MAX_IMG_H = 150;

    // Colors
    private static final int COLOR_BG       = 0xF21A1A2E;
    private static final int COLOR_HEADER   = 0xF2111130;
    private static final int COLOR_BORDER   = 0xFFFF69B4;
    private static final int COLOR_DIVIDER  = 0x88FF69B4;
    private static final int COLOR_BTN_BG   = 0xCC2A2A4E;
    private static final int COLOR_BTN_ROLL = 0xFF7B3F91;
    private static final int COLOR_TITLE    = 0xFFFF69B4;
    private static final int COLOR_GOLD     = 0xFFFFD700;
    private static final int COLOR_BLUE     = 0xFFADD8E6;
    private static final int COLOR_PURPLE   = 0xFFAA55FF;
    private static final int COLOR_GRAY     = 0xFF888888;
    private static final int COLOR_WHITE    = 0xFFFFFFFF;

    private State state = State.IDLE;
    private Character currentCharacter = null;
    private ImageInfo currentImage = null;
    private int playerKakera = 0;

    private Button rollWaifuBtn;
    private Button rollHusbandoBtn;
    private Button claimBtn;

    public MudaeScreen(MudaeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        int btnY = y + H - 38;

        rollWaifuBtn = addRenderableWidget(Button.builder(
            Component.literal("🎲 Waifu"), b -> doRoll(true))
            .pos(x + 8, btnY).size(82, 22).build());

        rollHusbandoBtn = addRenderableWidget(Button.builder(
            Component.literal("🎲 Husbando"), b -> doRoll(false))
            .pos(x + 96, btnY).size(90, 22).build());

        claimBtn = addRenderableWidget(Button.builder(
            Component.literal("💍 ¡Claim!"), b -> doClaim())
            .pos(x + 193, btnY).size(79, 22).build());
        claimBtn.active = false;
    }

    private void doRoll(boolean waifu) {
        state = State.ROLLING;
        currentCharacter = null;
        currentImage = null;
        claimBtn.active = false;
        rollWaifuBtn.active = false;
        rollHusbandoBtn.active = false;
        PacketDistributor.sendToServer(new RollRequestPayload(waifu));
    }

    private void doClaim() {
        if (currentCharacter == null) return;
        PacketDistributor.sendToServer(new ClaimRequestPayload(currentCharacter.id()));
        claimBtn.active = false;
    }

    public void onCharacterReceived(CharacterResultPayload payload) {
        this.currentCharacter = new Character(payload.id(), payload.name(), payload.animeName(), payload.imageUrl(), payload.kakeraValue());
        this.playerKakera = payload.playerKakera();
        this.state = State.LOADING_IMAGE;
        this.currentImage = null;
        rollWaifuBtn.active = true;
        rollHusbandoBtn.active = true;
        claimBtn.active = true;

        CharacterImageLoader.load(payload.id(), payload.imageUrl(), info -> {
            this.currentImage = info;
            if (this.state == State.LOADING_IMAGE) this.state = State.DONE;
        });
    }

    public void onRollError() {
        this.state = State.ERROR;
        rollWaifuBtn.active = true;
        rollHusbandoBtn.active = true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        int x = this.leftPos;
        int y = this.topPos;

        // Background
        g.fill(x, y, x + W, y + H, COLOR_BG);
        g.fill(x, y, x + W, y + 24, COLOR_HEADER);

        // Border (2px double border effect)
        g.hLine(x, x + W - 1, y, COLOR_BORDER);
        g.hLine(x, x + W - 1, y + 1, 0x44FF69B4);
        g.hLine(x, x + W - 1, y + H - 1, COLOR_BORDER);
        g.hLine(x, x + W - 1, y + H - 2, 0x44FF69B4);
        g.vLine(x, y, y + H, COLOR_BORDER);
        g.vLine(x + 1, y, y + H, 0x44FF69B4);
        g.vLine(x + W - 1, y, y + H, COLOR_BORDER);
        g.vLine(x + W - 2, y, y + H, 0x44FF69B4);

        // Title
        g.drawCenteredString(font, "✨  TERMINAL DE MUDAE  ✨", x + W / 2, y + 7, COLOR_TITLE);

        // Kakera display (top right)
        String kStr = "💎 " + playerKakera;
        g.fill(x + W - font.width(kStr) - 16, y + 4, x + W - 5, y + 18, 0x66000000);
        g.drawString(font, kStr, x + W - font.width(kStr) - 10, y + 7, COLOR_PURPLE, false);

        // Divider
        g.hLine(x + 5, x + W - 6, y + 24, COLOR_DIVIDER);

        // Button area background (makes buttons much more visible)
        g.fill(x + 5, y + H - 46, x + W - 5, y + H - 5, 0xBB0D0D20);
        g.hLine(x + 5, x + W - 6, y + H - 46, COLOR_DIVIDER);

        super.render(g, mx, my, delta);
        renderClaimTooltip(g, mx, my);

        renderContent(g, x, y);
    }

    private void renderContent(GuiGraphics g, int x, int y) {
        int contentY = y + 30;
        int contentH = H - 30 - 50;
        int centerX = x + W / 2;
        int centerY = contentY + contentH / 2;

        switch (state) {
            case IDLE -> {
                g.drawCenteredString(font, "Presioná  🎲 Waifu  o  🎲 Husbando", centerX, centerY - 10, COLOR_GRAY);
                g.drawCenteredString(font, "para invocar un personaje de anime!", centerX, centerY + 5, COLOR_GRAY);
            }
            case ROLLING -> {
                g.drawCenteredString(font, "⏳ Invocando desde AniList...", centerX, centerY, COLOR_GOLD);
            }
            case LOADING_IMAGE, DONE -> renderCharacter(g, x, y, contentY, contentH);
            case ERROR -> {
                g.drawCenteredString(font, "❌ No se pudo obtener un personaje.", centerX, centerY - 7, 0xFFFF5555);
                g.drawCenteredString(font, "Intentá de nuevo.", centerX, centerY + 8, COLOR_GRAY);
            }
        }
    }

    private void renderCharacter(GuiGraphics g, int x, int y, int contentY, int contentH) {
        if (currentCharacter == null) return;

        int imgX = x + 12;
        int imgY = contentY + 4;

        if (state == State.DONE && currentImage != null) {
            // Scale image to fit within MAX_IMG bounds, preserving aspect ratio
            float aspect = (float) currentImage.width() / currentImage.height();
            int drawW, drawH;
            if (aspect > (float) MAX_IMG_W / MAX_IMG_H) {
                drawW = MAX_IMG_W;
                drawH = (int) (MAX_IMG_W / aspect);
            } else {
                drawH = MAX_IMG_H;
                drawW = (int) (MAX_IMG_H * aspect);
            }

            // Shadow under image
            g.fill(imgX + 3, imgY + 3, imgX + drawW + 3, imgY + drawH + 3, 0x66000000);
            // Pink border around image
            g.fill(imgX - 2, imgY - 2, imgX + drawW + 2, imgY + drawH + 2, COLOR_BORDER);
            g.fill(imgX - 1, imgY - 1, imgX + drawW + 1, imgY + drawH + 1, 0xFF1A1A2E);

            // Draw image scaled
            g.pose().pushPose();
            float scaleX = (float) drawW / currentImage.width();
            float scaleY = (float) drawH / currentImage.height();
            g.pose().scale(scaleX, scaleY, 1f);
            g.blit(currentImage.location(),
                (int)(imgX / scaleX), (int)(imgY / scaleY),
                0f, 0f,
                currentImage.width(), currentImage.height(),
                currentImage.width(), currentImage.height());
            g.pose().popPose();

            imgX += drawW + 14;
        } else {
            // Loading placeholder with spinner feel
            g.fill(imgX, imgY, imgX + MAX_IMG_W, imgY + MAX_IMG_H, 0x44FFFFFF);
            g.fill(imgX + 1, imgY + 1, imgX + MAX_IMG_W - 1, imgY + MAX_IMG_H - 1, 0x221A1A3E);
            g.fill(imgX - 2, imgY - 2, imgX + MAX_IMG_W + 2, imgY + MAX_IMG_H + 2, COLOR_BORDER);
            g.fill(imgX - 1, imgY - 1, imgX + MAX_IMG_W + 1, imgY + MAX_IMG_H + 1, 0xFF1A1A2E);
            g.fill(imgX, imgY, imgX + MAX_IMG_W, imgY + MAX_IMG_H, 0x331A1A3E);
            g.drawCenteredString(font, "⏳ Cargando", imgX + MAX_IMG_W / 2, imgY + MAX_IMG_H / 2 - 4, COLOR_GRAY);
            imgX += MAX_IMG_W + 14;
        }

        // Info panel
        int tx = imgX;
        int ty = contentY + 4;
        int maxW = x + W - tx - 10;

        // Name
        String name = currentCharacter.name();
        if (font.width(name) > maxW) name = font.plainSubstrByWidth(name, maxW - 8) + "…";
        g.drawString(font, name, tx, ty, COLOR_GOLD, false);

        // Anime
        String anime = currentCharacter.animeName();
        if (font.width("📺 " + anime) > maxW) anime = font.plainSubstrByWidth(anime, maxW - 20) + "…";
        g.drawString(font, "📺 " + anime, tx, ty + 16, COLOR_BLUE, false);

        // Kakera value
        g.fill(tx - 2, ty + 33, tx + 100, ty + 47, 0x66000000);
        g.drawString(font, "💎 +" + currentCharacter.kakeraValue() + " kakera", tx, ty + 36, COLOR_PURPLE, false);

        // Claim window
        g.drawString(font, "⏳ 3 min para claimear", tx, ty + 55, COLOR_GRAY, false);

        // Divider line
        g.hLine(tx, tx + maxW, ty + 70, COLOR_DIVIDER);

        // Hint
        g.drawString(font, "✨ ¡Sé el primero en clickear Claim!", tx, ty + 76, 0xFFFFAA44, false);
    }

    private void renderClaimTooltip(GuiGraphics g, int mx, int my) {
        if (claimBtn != null && claimBtn.isHovered()) {
            g.renderTooltip(font, Component.literal("Claimear este personaje para tu harem"), mx, my);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float delta, int mx, int my) {}

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
