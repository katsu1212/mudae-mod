package com.mudaemod.mudaemod.gui;

import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.network.BuyStatPayload;
import com.mudaemod.mudaemod.network.HaremPayload;
import com.mudaemod.mudaemod.network.SellPayload;
import com.mudaemod.mudaemod.network.handler.MudaeClientHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MudaeScreen extends AbstractContainerScreen<MudaeMenu> {

    private static final int W = 320;
    private static final int H = 250;

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
    private static final int COLOR_GREEN   = 0xFF44FF44;
    private static final int COLOR_RED     = 0xFFFF5555;
    private static final int COLOR_SELL    = 0xFF884422;
    private static final int COLOR_BUY     = 0xFF224488;

    private static final int TAB_HAREM  = 0;
    private static final int TAB_TIENDA = 1;

    private int activeTab = TAB_HAREM;
    private int scrollOffset = 0;
    private static final int ROW_H = 22;
    private static final int LIST_Y_START = 46; // relative to topPos
    private static final int LIST_Y_END_OFFSET = 10; // px from bottom

    private HaremPayload haremData = null;

    public MudaeScreen(MudaeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        // Load last known harem data if available
        if (MudaeClientHandler.lastHaremData != null) {
            haremData = MudaeClientHandler.lastHaremData;
        }
    }

    public void onHaremReceived(HaremPayload payload) {
        this.haremData = payload;
        this.scrollOffset = 0;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        int x = this.leftPos;
        int y = this.topPos;

        // Background
        g.fill(x, y, x + W, y + H, COLOR_BG);
        g.fill(x, y, x + W, y + 24, COLOR_HEADER);

        // Border
        g.hLine(x, x + W - 1, y, COLOR_BORDER);
        g.hLine(x, x + W - 1, y + H - 1, COLOR_BORDER);
        g.vLine(x, y, y + H, COLOR_BORDER);
        g.vLine(x + W - 1, y, y + H, COLOR_BORDER);

        // Title
        if (haremData != null) {
            g.drawCenteredString(font, "✨  MUDAE  ✨", x + W / 2, y + 7, COLOR_TITLE);
            // Kakera
            String kStr = "💎 " + haremData.kakera();
            g.drawString(font, kStr, x + W - font.width(kStr) - 8, y + 7, COLOR_PURPLE, false);
        } else {
            g.drawCenteredString(font, "✨  MUDAE  ✨", x + W / 2, y + 7, COLOR_TITLE);
            g.drawString(font, "Cargando...", x + 8, y + 7, COLOR_GRAY, false);
        }

        // Tab buttons
        drawTab(g, x, y, TAB_HAREM,  "📦 Harem");
        drawTab(g, x, y, TAB_TIENDA, "🛒 Tienda");
        g.hLine(x + 5, x + W - 6, y + 42, COLOR_DIVIDER);

        super.render(g, mx, my, delta);

        // Content
        if (activeTab == TAB_HAREM) renderHaremTab(g, x, y, mx, my);
        else renderTiendaTab(g, x, y, mx, my);
    }

    private void drawTab(GuiGraphics g, int x, int y, int tab, String label) {
        int tabW = 100;
        int tabX = x + 5 + tab * (tabW + 4);
        int tabY = y + 25;
        boolean active = activeTab == tab;
        g.fill(tabX, tabY, tabX + tabW, tabY + 16,
            active ? 0xFF222244 : 0xFF111133);
        if (active) {
            g.hLine(tabX, tabX + tabW - 1, tabY, COLOR_BORDER);
            g.vLine(tabX, tabY, tabY + 16, COLOR_BORDER);
            g.vLine(tabX + tabW - 1, tabY, tabY + 16, COLOR_BORDER);
        }
        g.drawCenteredString(font, label, tabX + tabW / 2, tabY + 4,
            active ? COLOR_TITLE : COLOR_GRAY);
    }

    private void renderHaremTab(GuiGraphics g, int x, int y, int mx, int my) {
        List<HaremPayload.HaremEntry> entries = haremData != null ? haremData.entries() : new ArrayList<>();

        if (entries.isEmpty()) {
            g.drawCenteredString(font, "Tu harem está vacío.", x + W / 2, y + H / 2, COLOR_GRAY);
            g.drawCenteredString(font, "Usá $w o $h en el chat para invocar personajes.", x + W / 2, y + H / 2 + 14, COLOR_GRAY);
            return;
        }

        int listY = y + LIST_Y_START;
        int listH = H - LIST_Y_START - LIST_Y_END_OFFSET;
        int visibleRows = listH / ROW_H;
        int maxScroll = Math.max(0, entries.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // Clip indicator
        if (entries.size() > visibleRows) {
            g.drawString(font, "▲ " + scrollOffset, x + W - 30, listY, COLOR_GRAY, false);
        }

        for (int i = 0; i < visibleRows && (scrollOffset + i) < entries.size(); i++) {
            HaremPayload.HaremEntry entry = entries.get(scrollOffset + i);
            int ry = listY + i * ROW_H;
            boolean hovered = mx >= x + 5 && mx < x + W - 5 && my >= ry && my < ry + ROW_H - 1;

            // Row background
            g.fill(x + 5, ry, x + W - 5, ry + ROW_H - 2, hovered ? 0x33FFFFFF : 0x22000000);

            // Name
            String name = entry.name();
            if (font.width(name) > 100) name = font.plainSubstrByWidth(name, 97) + "…";
            g.drawString(font, name, x + 8, ry + 5, COLOR_GOLD, false);

            // Anime
            String anime = entry.animeName();
            if (font.width(anime) > 88) anime = font.plainSubstrByWidth(anime, 85) + "…";
            g.drawString(font, anime, x + 114, ry + 5, COLOR_BLUE, false);

            // Kakera (right-aligned before button)
            String kak = "+" + entry.kakeraValue();
            g.drawString(font, kak, x + 208, ry + 5, COLOR_PURPLE, false);

            // Vender button
            int btnX = x + W - 56;
            int btnY2 = ry + 2;
            boolean btnHov = mx >= btnX && mx < btnX + 48 && my >= btnY2 && my < btnY2 + 16;
            g.fill(btnX, btnY2, btnX + 48, btnY2 + 16, btnHov ? 0xFFAA5533 : COLOR_SELL);
            g.drawCenteredString(font, "Vender", btnX + 24, btnY2 + 4, COLOR_WHITE);

            g.hLine(x + 5, x + W - 6, ry + ROW_H - 2, 0x33FF69B4);
        }
    }

    private void renderTiendaTab(GuiGraphics g, int x, int y, int mx, int my) {
        int[] statLevels = new int[]{0, 0, 0, 0, 0};
        if (haremData != null) {
            statLevels = new int[]{haremData.statVida(), haremData.statVel(), haremData.statFuerza(), haremData.statDef(), haremData.statMina()};
        }

        String[] icons = {"❤", "⚡", "⚔", "🛡", "⛏"};
        String[] descs = {"+1 corazon / nivel", "+0.01 vel / nivel", "+0.5 atk / nivel", "+1 armor / nivel", "+1 vel.mineria / nivel"};

        int startY = y + LIST_Y_START;
        for (int i = 0; i < 5; i++) {
            int ry = startY + i * 38;
            int lv = statLevels[i];
            int cost = PlayerData.getStatCost(i, lv);
            boolean maxed = lv >= PlayerData.STAT_MAX[i];

            g.fill(x + 8, ry, x + W - 8, ry + 32, 0x33000000);
            g.hLine(x + 8, x + W - 9, ry, COLOR_DIVIDER);

            // Stat name + icon
            g.drawString(font, icons[i] + " " + PlayerData.STAT_NAMES[i], x + 14, ry + 4, COLOR_GOLD, false);

            // Desc
            g.drawString(font, descs[i], x + 14, ry + 16, COLOR_GRAY, false);

            // Level bar (pip count = max for this stat)
            int maxLv = PlayerData.STAT_MAX[i];
            int pipW = maxLv <= 5 ? 14 : 8;
            for (int p = 0; p < maxLv; p++) {
                int px = x + 155 + p * (pipW + 1);
                int py2 = ry + 8;
                g.fill(px, py2, px + pipW - 1, py2 + 8, p < lv ? COLOR_GREEN : 0xFF333355);
                g.hLine(px, px + pipW - 2, py2, COLOR_BORDER);
                g.hLine(px, px + pipW - 2, py2 + 7, COLOR_BORDER);
                g.vLine(px, py2, py2 + 8, COLOR_BORDER);
                g.vLine(px + pipW - 1, py2, py2 + 8, COLOR_BORDER);
            }

            // Buy button
            int btnX = x + W - 62;
            int btnY2 = ry + 8;
            if (!maxed) {
                boolean hover = mx >= btnX && mx < btnX + 54 && my >= btnY2 && my < btnY2 + 16;
                g.fill(btnX, btnY2, btnX + 54, btnY2 + 16, hover ? 0xFF3366AA : COLOR_BUY);
                g.drawCenteredString(font, "💎" + cost, btnX + 27, btnY2 + 4, COLOR_WHITE);
            } else {
                g.fill(btnX, btnY2, btnX + 54, btnY2 + 16, 0xFF222233);
                g.drawCenteredString(font, "MAX", btnX + 27, btnY2 + 4, COLOR_GRAY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int x = this.leftPos;
            int y = this.topPos;

            // Tab click detection
            for (int t = 0; t < 2; t++) {
                int tabX = x + 5 + t * 104;
                int tabY = y + 25;
                if (mx >= tabX && mx < tabX + 100 && my >= tabY && my < tabY + 16) {
                    activeTab = t;
                    scrollOffset = 0;
                    return true;
                }
            }

            if (activeTab == TAB_HAREM) {
                return handleHaremClick(mx, my, x, y);
            } else {
                return handleTiendaClick(mx, my, x, y);
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean handleHaremClick(double mx, double my, int x, int y) {
        if (haremData == null || haremData.entries().isEmpty()) return false;
        int listY = y + LIST_Y_START;
        int listH = H - LIST_Y_START - LIST_Y_END_OFFSET;
        int visibleRows = listH / ROW_H;
        List<HaremPayload.HaremEntry> entries = haremData.entries();

        for (int i = 0; i < visibleRows && (scrollOffset + i) < entries.size(); i++) {
            int ry = listY + i * ROW_H;
            int btnX = x + W - 56;
            int btnY2 = ry + 2;
            if (mx >= btnX && mx < btnX + 48 && my >= btnY2 && my < btnY2 + 16) {
                HaremPayload.HaremEntry entry = entries.get(scrollOffset + i);
                PacketDistributor.sendToServer(new SellPayload(entry.id()));
                return true;
            }
        }
        return false;
    }

    private boolean handleTiendaClick(double mx, double my, int x, int y) {
        int startY = y + LIST_Y_START;
        int[] statLevels = haremData != null
            ? new int[]{haremData.statVida(), haremData.statVel(), haremData.statFuerza(), haremData.statDef(), haremData.statMina()}
            : new int[]{0, 0, 0, 0, 0};

        for (int i = 0; i < 5; i++) {
            if (statLevels[i] >= PlayerData.STAT_MAX[i]) continue;
            int ry = startY + i * 38;
            int btnX = x + W - 62;
            int btnY2 = ry + 8;
            if (mx >= btnX && mx < btnX + 54 && my >= btnY2 && my < btnY2 + 16) {
                PacketDistributor.sendToServer(new BuyStatPayload(i));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (activeTab == TAB_HAREM) {
            int entries = haremData != null ? haremData.entries().size() : 0;
            int listH = H - LIST_Y_START - LIST_Y_END_OFFSET;
            int visible = listH / ROW_H;
            int maxScroll = Math.max(0, entries - visible);
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int) Math.signum(scrollY), maxScroll));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float delta, int mx, int my) {}

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
