package com.mudaemod.mudaemod.gui;

import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.network.CharacterImageLoader;
import com.mudaemod.mudaemod.network.CharacterResultPayload;
import com.mudaemod.mudaemod.network.ClaimRequestPayload;
import com.mudaemod.mudaemod.network.RollRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class MudaeScreen extends AbstractContainerScreen<MudaeMenu> {

    private enum State { IDLE, ROLLING, LOADING_IMAGE, DONE, ERROR }

    private static final int W = 260;
    private static final int H = 210;
    private static final int IMG_SIZE = 110;

    private State state = State.IDLE;
    private Character currentCharacter = null;
    private ResourceLocation characterTexture = null;
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

        rollWaifuBtn = addRenderableWidget(Button.builder(
            Component.literal("🎲 Waifu"), b -> doRoll(true))
            .pos(x + 10, y + H - 45).size(75, 20).build());

        rollHusbandoBtn = addRenderableWidget(Button.builder(
            Component.literal("🎲 Husbando"), b -> doRoll(false))
            .pos(x + 90, y + H - 45).size(85, 20).build());

        claimBtn = addRenderableWidget(Button.builder(
            Component.literal("💍 Claim"), b -> doClaim())
            .pos(x + 180, y + H - 45).size(70, 20).build());
        claimBtn.active = false;
    }

    private void doRoll(boolean waifu) {
        state = State.ROLLING;
        currentCharacter = null;
        characterTexture = null;
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

    /** Called by network handler when server responds with a character. */
    public void onCharacterReceived(CharacterResultPayload payload) {
        this.currentCharacter = new Character(payload.id(), payload.name(), payload.animeName(), payload.imageUrl());
        this.playerKakera = payload.playerKakera();
        this.state = State.LOADING_IMAGE;
        this.characterTexture = null;
        rollWaifuBtn.active = true;
        rollHusbandoBtn.active = true;
        claimBtn.active = true;

        CharacterImageLoader.load(payload.id(), payload.imageUrl(), tex -> {
            this.characterTexture = tex;
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
        renderBackground(g, mx, my, delta);
        super.render(g, mx, my, delta);

        int x = this.leftPos;
        int y = this.topPos;

        // Background
        g.fill(x, y, x + W, y + H, 0xEE0D0D1A);
        g.fill(x, y, x + W, y + 22, 0xEE111130);
        // Border
        g.hLine(x, x + W - 1, y, 0xFFFF69B4);
        g.hLine(x, x + W - 1, y + H - 1, 0xFFFF69B4);
        g.vLine(x, y, y + H - 1, 0xFFFF69B4);
        g.vLine(x + W - 1, y, y + H - 1, 0xFFFF69B4);

        // Title
        g.drawCenteredString(font, "✨ ALTAR DE MUDAE ✨", x + W / 2, y + 7, 0xFFFF69B4);

        // Kakera counter (top right)
        g.drawString(font, "💎 " + playerKakera, x + W - 60, y + 7, 0xFFAA55FF, false);

        // Divider
        g.hLine(x + 5, x + W - 6, y + 22, 0x88FF69B4);

        renderContent(g, x, y);
        renderTooltips(g, mx, my);
    }

    private void renderContent(GuiGraphics g, int x, int y) {
        int centerY = y + 30 + (H - 85) / 2;

        switch (state) {
            case IDLE -> {
                g.drawCenteredString(font, "Presioná un botón para invocar", x + W / 2, centerY - 10, 0xAAAAAA);
                g.drawCenteredString(font, "un personaje de anime!", x + W / 2, centerY + 5, 0xAAAAAA);
            }
            case ROLLING -> {
                g.drawCenteredString(font, "🎲 Invocando personaje...", x + W / 2, centerY, 0xFFD700);
            }
            case LOADING_IMAGE, DONE -> renderCharacter(g, x, y);
            case ERROR -> {
                g.drawCenteredString(font, "❌ No se pudo obtener un personaje.", x + W / 2, centerY, 0xFF5555);
                g.drawCenteredString(font, "Intentá de nuevo.", x + W / 2, centerY + 14, 0xAAAAAA);
            }
        }
    }

    private void renderCharacter(GuiGraphics g, int x, int y) {
        if (currentCharacter == null) return;

        int imgX = x + 15;
        int imgY = y + 30;

        if (state == State.DONE && characterTexture != null) {
            g.blit(characterTexture, imgX, imgY, 0f, 0f, IMG_SIZE, IMG_SIZE, IMG_SIZE, IMG_SIZE);
        } else {
            // Loading placeholder
            g.fill(imgX, imgY, imgX + IMG_SIZE, imgY + IMG_SIZE, 0x33FFFFFF);
            g.fill(imgX + 1, imgY + 1, imgX + IMG_SIZE - 1, imgY + IMG_SIZE - 1, 0x22AAAAFF);
            g.drawCenteredString(font, "⏳", imgX + IMG_SIZE / 2, imgY + IMG_SIZE / 2 - 4, 0xFFFFFF);
        }

        // Info panel on the right
        int tx = imgX + IMG_SIZE + 12;
        int ty = imgY + 5;
        int maxWidth = W - IMG_SIZE - 35;

        // Name (truncate if too long)
        String name = currentCharacter.name();
        if (font.width(name) > maxWidth) name = font.plainSubstrByWidth(name, maxWidth - 6) + "…";
        g.drawString(font, name, tx, ty, 0xFFD700, false);

        // Anime
        String anime = "📺 " + currentCharacter.animeName();
        if (font.width(anime) > maxWidth) anime = font.plainSubstrByWidth(anime, maxWidth - 6) + "…";
        g.drawString(font, anime, tx, ty + 16, 0xADD8E6, false);

        // Kakera value
        g.drawString(font, "💎 +" + currentCharacter.kakeraValue() + " kakera", tx, ty + 32, 0xAA55FF, false);

        // Claim window hint
        g.drawString(font, "⏳ 3 min para claimear", tx, ty + 52, 0x888888, false);
    }

    private void renderTooltips(GuiGraphics g, int mx, int my) {
        // Could add tooltips later
    }

    @Override
    protected void renderBg(GuiGraphics g, float delta, int mx, int my) {}

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
