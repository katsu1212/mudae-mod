package com.mudaemod.mudaemod.gui;

import com.mojang.math.Axis;
import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.network.CharacterResultPayload;
import com.mudaemod.mudaemod.network.ClaimRequestPayload;
import com.mudaemod.mudaemod.network.RollRequestPayload;
import com.mudaemod.mudaemod.network.SkinLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class MudaeScreen extends AbstractContainerScreen<MudaeMenu> {

    private enum State { IDLE, ROLLING, DONE, ERROR }

    private static final int W = 310;
    private static final int H = 220;

    // Model display box (left column)
    private static final int MODEL_BOX_X = 5;
    private static final int MODEL_BOX_Y = 28;
    private static final int MODEL_BOX_W = 128;
    private static final int MODEL_BOX_H = 128;
    private static final int MODEL_FEET_X = 69; // center of model box
    private static final int MODEL_FEET_Y = 185; // feet Y relative to topPos

    // Colors
    private static final int COLOR_BG      = 0xF21A1A2E;
    private static final int COLOR_HEADER  = 0xF2111130;
    private static final int COLOR_BORDER  = 0xFFFF69B4;
    private static final int COLOR_DIVIDER = 0x88FF69B4;
    private static final int COLOR_TITLE   = 0xFFFF69B4;
    private static final int COLOR_GOLD    = 0xFFFFD700;
    private static final int COLOR_BLUE    = 0xFFADD8E6;
    private static final int COLOR_PURPLE  = 0xFFAA55FF;
    private static final int COLOR_GRAY    = 0xFF888888;
    private static final int COLOR_WHITE   = 0xFFFFFFFF;

    private State state = State.IDLE;
    private Character currentCharacter = null;
    private RemotePlayer fakePlayer = null;
    private int playerKakera = 0;

    // Slot machine spin animation
    private float spinAngle = 0f;
    private float spinSpeed = 0f;

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
            .pos(x + 224, btnY).size(78, 22).build());
        claimBtn.active = false;
    }

    private void doRoll(boolean waifu) {
        state = State.ROLLING;
        currentCharacter = null;
        fakePlayer = null;
        spinSpeed = 20f; // fast spin during roll
        spinAngle = 0f;
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
        this.currentCharacter = new Character(
            payload.id(), payload.name(), payload.animeName(), payload.skinUUID(), payload.kakeraValue());
        this.playerKakera = payload.playerKakera();
        this.state = State.DONE;
        this.fakePlayer = null;
        rollWaifuBtn.active = true;
        rollHusbandoBtn.active = true;
        claimBtn.active = true;

        // Load skin via Minecraft's own SkinManager — no custom HTTP, always works
        SkinLoader.createPlayer(payload.skinUUID(), payload.name(), payload.id()).thenAccept(player ->
            Minecraft.getInstance().execute(() -> this.fakePlayer = player)
        );
    }

    public void onRollError() {
        this.state = State.ERROR;
        this.spinSpeed = 0f;
        rollWaifuBtn.active = true;
        rollHusbandoBtn.active = true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        int x = this.leftPos;
        int y = this.topPos;

        // Update spin animation
        if (state == State.ROLLING) {
            spinAngle += spinSpeed;
        } else if (state == State.DONE) {
            // Decelerate from fast roll spin to slow idle spin
            spinSpeed = spinSpeed > 1.5f ? spinSpeed * 0.93f : 1.5f;
            spinAngle += spinSpeed;
        }

        // Background
        g.fill(x, y, x + W, y + H, COLOR_BG);
        g.fill(x, y, x + W, y + 24, COLOR_HEADER);

        // Border
        g.hLine(x, x + W - 1, y, COLOR_BORDER);
        g.hLine(x, x + W - 1, y + H - 1, COLOR_BORDER);
        g.vLine(x, y, y + H, COLOR_BORDER);
        g.vLine(x + W - 1, y, y + H, COLOR_BORDER);

        // Title
        g.drawCenteredString(font, "✨  TERMINAL DE MUDAE  ✨", x + W / 2, y + 7, COLOR_TITLE);

        // Kakera display
        String kStr = "💎 " + playerKakera;
        g.drawString(font, kStr, x + W - font.width(kStr) - 10, y + 7, COLOR_PURPLE, false);

        // Divider under header
        g.hLine(x + 5, x + W - 6, y + 24, COLOR_DIVIDER);

        // Button area background
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
                renderModelBox(g, x, y);
                // Spinning placeholder silhouette
                g.drawCenteredString(font, "⏳", x + MODEL_BOX_X + MODEL_BOX_W / 2, y + MODEL_BOX_Y + MODEL_BOX_H / 2 - 4, COLOR_GOLD);
                g.drawCenteredString(font, "Invocando...", x + MODEL_BOX_X + MODEL_BOX_W / 2, y + MODEL_BOX_Y + MODEL_BOX_H / 2 + 12, COLOR_GOLD);
            }
            case DONE -> renderCharacter(g, x, y, contentY);
            case ERROR -> {
                g.drawCenteredString(font, "❌ No se pudo obtener un personaje.", centerX, centerY - 7, 0xFFFF5555);
                g.drawCenteredString(font, "Intentá de nuevo.", centerX, centerY + 8, COLOR_GRAY);
            }
        }
    }

    private void renderModelBox(GuiGraphics g, int x, int y) {
        int bx = x + MODEL_BOX_X;
        int by = y + MODEL_BOX_Y;

        // Dark inner background for model display
        g.fill(bx, by, bx + MODEL_BOX_W, by + MODEL_BOX_H, 0x55000000);
        g.fill(bx + 1, by + 1, bx + MODEL_BOX_W - 1, by + MODEL_BOX_H - 1, 0x221A1A4E);

        // Pink glow border
        g.hLine(bx, bx + MODEL_BOX_W - 1, by, COLOR_BORDER);
        g.hLine(bx, bx + MODEL_BOX_W - 1, by + MODEL_BOX_H - 1, COLOR_BORDER);
        g.vLine(bx, by, by + MODEL_BOX_H, COLOR_BORDER);
        g.vLine(bx + MODEL_BOX_W - 1, by, by + MODEL_BOX_H, COLOR_BORDER);

        // Corner accents
        g.fill(bx, by, bx + 4, by + 4, COLOR_BORDER);
        g.fill(bx + MODEL_BOX_W - 4, by, bx + MODEL_BOX_W, by + 4, COLOR_BORDER);
        g.fill(bx, by + MODEL_BOX_H - 4, bx + 4, by + MODEL_BOX_H, COLOR_BORDER);
        g.fill(bx + MODEL_BOX_W - 4, by + MODEL_BOX_H - 4, bx + MODEL_BOX_W, by + MODEL_BOX_H, COLOR_BORDER);
    }

    private void renderCharacter(GuiGraphics g, int x, int y, int contentY) {
        if (currentCharacter == null) return;

        renderModelBox(g, x, y);

        // --- 3D rotating player model ---
        if (fakePlayer != null) {
            try {
                // Sync tick for walking animation
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) fakePlayer.tickCount = mc.player.tickCount;

                // Camera: flip upright (ZP 180°) + slight downward tilt
                Quaternionf camera = Axis.ZP.rotationDegrees(180.0f);
                camera.mul(Axis.XP.rotationDegrees(10.0f));

                // Entity: Y-axis spin
                Quaternionf entitySpin = Axis.YP.rotationDegrees(spinAngle);

                InventoryScreen.renderEntityInInventory(
                    g,
                    (float)(x + MODEL_BOX_X + MODEL_BOX_W / 2),
                    (float)(y + MODEL_FEET_Y),
                    45,                   // scale
                    new Vector3f(0, 0.5f, 0),
                    camera,
                    entitySpin,
                    fakePlayer
                );
            } catch (Exception e) {
                // Fallback if rendering fails
                g.drawCenteredString(font, "?", x + MODEL_BOX_X + MODEL_BOX_W / 2,
                    y + MODEL_BOX_Y + MODEL_BOX_H / 2, COLOR_GRAY);
            }
        } else {
            // Skin still loading — show spinner
            g.drawCenteredString(font, "⟳ cargando skin",
                x + MODEL_BOX_X + MODEL_BOX_W / 2,
                y + MODEL_BOX_Y + MODEL_BOX_H / 2, COLOR_GRAY);
        }

        // Vertical divider between model and info
        g.vLine(x + MODEL_BOX_X + MODEL_BOX_W + 3, y + 28, y + H - 48, COLOR_DIVIDER);

        // --- Info panel (right of model) ---
        int tx = x + MODEL_BOX_X + MODEL_BOX_W + 10;
        int ty = contentY + 4;
        int maxW = W - (MODEL_BOX_X + MODEL_BOX_W + 14);

        // Character name
        String name = currentCharacter.name();
        if (font.width(name) > maxW) name = font.plainSubstrByWidth(name, maxW - 8) + "…";
        g.drawString(font, name, tx, ty, COLOR_GOLD, false);

        // Anime source
        String anime = currentCharacter.animeName();
        String animeLabel = "📺 " + anime;
        if (font.width(animeLabel) > maxW) animeLabel = "📺 " + font.plainSubstrByWidth(anime, maxW - 22) + "…";
        g.drawString(font, animeLabel, tx, ty + 16, COLOR_BLUE, false);

        // Kakera value badge
        g.fill(tx - 2, ty + 33, tx + 112, ty + 47, 0x66000000);
        g.drawString(font, "💎 +" + currentCharacter.kakeraValue() + " kakera", tx, ty + 36, COLOR_PURPLE, false);

        // Claim timer hint
        g.drawString(font, "⏳ 3 min para claimear", tx, ty + 55, COLOR_GRAY, false);

        // Decorative divider
        g.hLine(tx, tx + maxW - 4, ty + 70, COLOR_DIVIDER);

        // Call to action
        g.drawString(font, "💍 ¡Sé el primero en Claim!", tx, ty + 78, 0xFFFFAA44, false);
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
