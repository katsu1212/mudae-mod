package com.mudaemod.mudaemod.gui;

import com.mudaemod.mudaemod.network.CasinoActionPayload;
import com.mudaemod.mudaemod.network.CasinoBetPayload;
import com.mudaemod.mudaemod.network.CasinoResultPayload;
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
public class CasinoScreen extends AbstractContainerScreen<CasinoMenu> {

    private static final int W = 360;
    private static final int H = 260;

    // Colors
    private static final int BG       = 0xFF0D0D1A;
    private static final int HEADER   = 0xFF1A0D2E;
    private static final int GOLD     = 0xFFFFD700;
    private static final int WHITE    = 0xFFFFFFFF;
    private static final int GRAY     = 0xFF666688;
    private static final int GREEN    = 0xFF44FF88;
    private static final int RED      = 0xFFFF4444;
    private static final int CYAN     = 0xFF44DDFF;
    private static final int PURPLE   = 0xFFAA55FF;
    private static final int ORANGE   = 0xFFFF8800;
    private static final int DARK_BTN = 0xFF222244;
    private static final int DARK_SEL = 0xFF332255;
    private static final int BTN_BORD = 0xFF4444AA;

    private static final int TAB_SLOTS    = 0;
    private static final int TAB_ROULETTE = 1;
    private static final int TAB_BLACKJACK = 2;
    private int activeTab = TAB_SLOTS;

    // Bet selector: 50,100,250,500,1000
    private static final int[] BET_OPTIONS = {50, 100, 250, 500, 1000};
    private int selectedBet = 0; // index into BET_OPTIONS

    // Roulette bet type
    private static final String[] ROUL_LABELS = {"ROJO","NEGRO","PAR","IMPAR","1-18","19-36","1aDoc","2aDoc","3aDoc"};
    private static final int[]    ROUL_COLORS  = {RED, 0xFF333333, WHITE, WHITE, WHITE, WHITE, PURPLE, PURPLE, PURPLE};
    private int selectedRoulBet = 0;

    // Slots symbols display
    private static final String[] SYM_LABELS = {"BAR","LIM","NAR","STR","GEM","777"};
    private static final int[]    SYM_COLORS  = {RED, 0xFFFFFF00, ORANGE, GOLD, CYAN, GOLD};

    // State
    private int myKakera = 0;
    private int[] slotResult  = null;   // [s1,s2,s3]
    private int   roulResult  = -1;
    private int[] bjData      = null;   // encoded blackjack state
    private String lastMsg    = "";
    private int   lastWinDelta = 0;

    public CasinoScreen(CasinoMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = W;
        this.imageHeight = H;
    }

    public void onResult(CasinoResultPayload p) {
        myKakera    = p.newKakera();
        lastMsg     = p.msg();
        lastWinDelta = p.winDelta();
        if (p.gameType() == 0 && p.data().length >= 3) slotResult = p.data();
        if (p.gameType() == 1 && p.data().length >= 1) roulResult  = p.data()[0];
        if (p.gameType() == 2) bjData = p.data();
    }

    @Override
    protected void init() {
        super.init();
        // Load kakera from client handler if available
        var last = com.mudaemod.mudaemod.network.handler.MudaeClientHandler.lastHaremData;
        if (last != null) myKakera = last.kakera();
    }

    // ─── Render ────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        int x = leftPos, y = topPos;

        // Background
        g.fill(x, y, x+W, y+H, BG);
        g.fill(x, y, x+W, y+20, HEADER);

        // Title
        g.drawString(font, "* Casino de Mudae *", x+W/2 - font.width("* Casino de Mudae *")/2, y+6, GOLD, false);

        // Kakera display
        String kStr = "Kakera: " + myKakera;
        g.drawString(font, kStr, x+W - font.width(kStr) - 6, y+6, CYAN, false);

        // Tabs
        drawTabs(g, x, y, mx, my);

        // Tab content
        int cy = y + 38;
        if (activeTab == TAB_SLOTS)     renderSlots(g, x, cy, mx, my);
        if (activeTab == TAB_ROULETTE)  renderRoulette(g, x, cy, mx, my);
        if (activeTab == TAB_BLACKJACK) renderBlackjack(g, x, cy, mx, my);

        // Result message at bottom
        if (!lastMsg.isEmpty()) {
            int msgColor = lastWinDelta > 0 ? GREEN : lastWinDelta < 0 ? RED : GOLD;
            g.drawString(font, lastMsg, x+W/2 - font.width(lastMsg)/2, y+H-14, msgColor, true);
        }
    }

    private void drawTabs(GuiGraphics g, int x, int y, int mx, int my) {
        String[] tabs = {"SLOTS","RULETA","BLACKJACK"};
        int tw = W / 3;
        for (int i = 0; i < 3; i++) {
            int tx = x + i*tw, ty = y+20;
            int bg = (i == activeTab) ? DARK_SEL : DARK_BTN;
            int col = (i == activeTab) ? GOLD : GRAY;
            g.fill(tx, ty, tx+tw, ty+16, bg);
            g.fill(tx, ty+15, tx+tw, ty+16, i == activeTab ? GOLD : BTN_BORD);
            g.drawString(font, tabs[i], tx + tw/2 - font.width(tabs[i])/2, ty+4, col, false);
        }
    }

    // ─── SLOTS ─────────────────────────────────────────────────────────────
    private void renderSlots(GuiGraphics g, int x, int y, int mx, int my) {
        // 3 reels
        int reelW = 60, reelH = 54, gap = 12;
        int totalW = 3*reelW + 2*gap;
        int rx = x + (W - totalW)/2;
        for (int i = 0; i < 3; i++) {
            int rx2 = rx + i*(reelW+gap);
            g.fill(rx2, y, rx2+reelW, y+reelH, DARK_BTN);
            g.fill(rx2, y, rx2+1, y+reelH, GOLD);
            g.fill(rx2+reelW-1, y, rx2+reelW, y+reelH, GOLD);
            g.fill(rx2, y, rx2+reelW, y+1, GOLD);
            g.fill(rx2, y+reelH-1, rx2+reelW, y+reelH, GOLD);
            if (slotResult != null && i < slotResult.length) {
                int sym = slotResult[i];
                String lbl = SYM_LABELS[sym];
                int col = SYM_COLORS[sym];
                // Big symbol text
                g.drawString(font, lbl, rx2 + reelW/2 - font.width(lbl)/2, y + reelH/2 - 4, col, true);
            } else {
                g.drawString(font, "?", rx2 + reelW/2 - font.width("?")/2, y + reelH/2 - 4, GRAY, false);
            }
        }

        // Bet selector
        drawBetSelector(g, x, y+62, mx, my);

        // SPIN button
        drawBtn(g, x + W/2 - 40, y+100, 80, 16, "GIRAR", isHover(mx,my, x+W/2-40, y+100, 80, 16) ? GOLD : WHITE, GREEN);
    }

    // ─── ROULETTE ──────────────────────────────────────────────────────────
    private void renderRoulette(GuiGraphics g, int x, int y, int mx, int my) {
        // Wheel result display
        int wy = y+2;
        String spinTxt = roulResult >= 0 ? String.valueOf(roulResult) : "?";
        int spinCol = roulResult < 0 ? GRAY : roulResult == 0 ? GREEN : isRed(roulResult) ? RED : WHITE;
        g.fill(x + W/2 - 24, wy, x + W/2 + 24, wy + 30, DARK_BTN);
        g.fill(x + W/2 - 25, wy, x + W/2 - 24, wy+30, spinCol);
        g.fill(x + W/2 + 24, wy, x + W/2 + 25, wy+30, spinCol);
        g.fill(x + W/2 - 24, wy, x + W/2 + 24, wy+1, spinCol);
        g.fill(x + W/2 - 24, wy+29, x + W/2 + 24, wy+30, spinCol);
        // Scale up number text
        g.drawString(font, spinTxt, x + W/2 - font.width(spinTxt)/2, wy+11, spinCol, true);

        // Bet type buttons (3 rows × 3)
        String[] rows1 = {"ROJO","NEGRO"};
        String[] rows2 = {"PAR","IMPAR","1-18","19-36"};
        String[] rows3 = {"1a Doc","2a Doc","3a Doc"};

        int by1 = y+36;
        // Row 1: Red / Black (2-col)
        for (int i = 0; i < 2; i++) {
            int bx = x + 10 + i*(W/2 - 15);
            int bw = W/2 - 20;
            boolean sel = (i == 0 && selectedRoulBet==0) || (i==1 && selectedRoulBet==1);
            int col = i==0 ? RED : 0xFF888888;
            drawSelectBtn(g, bx, by1, bw, 14, rows1[i], sel, col, mx, my);
        }
        // Row 2: Even/Odd/Low/High
        int by2 = by1 + 18;
        int[] r2idx = {2,3,4,5};
        int bw2 = (W - 20)/4 - 2;
        for (int i = 0; i < 4; i++) {
            int bx = x + 10 + i*(bw2+2);
            boolean sel = selectedRoulBet == r2idx[i];
            drawSelectBtn(g, bx, by2, bw2, 14, rows2[i], sel, PURPLE, mx, my);
        }
        // Row 3: Dozens
        int by3 = by2 + 18;
        int bw3 = (W - 20)/3 - 2;
        int[] r3idx = {6,7,8};
        for (int i = 0; i < 3; i++) {
            int bx = x + 10 + i*(bw3+2);
            boolean sel = selectedRoulBet == r3idx[i];
            drawSelectBtn(g, bx, by3, bw3, 14, rows3[i], sel, CYAN, mx, my);
        }

        // Bet selector
        drawBetSelector(g, x, by3+18, mx, my);

        // GIRAR button
        drawBtn(g, x + W/2 - 40, by3+36, 80, 16, "GIRAR", isHover(mx,my, x+W/2-40, by3+36, 80, 16) ? GOLD : WHITE, GREEN);
    }

    // ─── BLACKJACK ─────────────────────────────────────────────────────────
    private void renderBlackjack(GuiGraphics g, int x, int y, int mx, int my) {
        // Parse hand data
        List<Integer> dealerCards = new ArrayList<>();
        List<Integer> playerCards = new ArrayList<>();
        int gameState = BlackjackState.PLAYING;
        boolean dealerRevealed = false;

        if (bjData != null && bjData.length > 1) {
            gameState = bjData[0];
            dealerRevealed = bjData[1] == 1;
            boolean inDealer = true;
            for (int i = 2; i < bjData.length; i++) {
                if (bjData[i] == -1) { inDealer = false; continue; }
                if (inDealer) dealerCards.add(bjData[i]);
                else          playerCards.add(bjData[i]);
            }
        }

        // Dealer hand
        g.drawString(font, "Dealer:", x+8, y, GRAY, false);
        renderHand(g, x+60, y, dealerCards, dealerRevealed ? dealerCards.size() : 1);
        if (!dealerCards.isEmpty()) {
            int dv = dealerRevealed ? handValue(dealerCards) : cardValue(dealerCards.get(0));
            String dvs = dealerRevealed ? String.valueOf(dv) : dv + "+?";
            g.drawString(font, "(" + dvs + ")", x + 60 + dealerCards.size()*18 + 4, y, GRAY, false);
        }

        // Player hand
        int phy = y + 34;
        g.drawString(font, "Vos:", x+8, phy, WHITE, false);
        renderHand(g, x+60, phy, playerCards, playerCards.size());
        if (!playerCards.isEmpty()) {
            int pv = handValue(playerCards);
            g.drawString(font, "(" + pv + ")", x + 60 + playerCards.size()*18 + 4, phy, pv > 21 ? RED : GREEN, false);
        }

        // Bet selector
        int bsy = y + 72;
        drawBetSelector(g, x, bsy, mx, my);

        // Action buttons
        int aby = bsy + 18;
        boolean playing = gameState == BlackjackState.PLAYING && !playerCards.isEmpty();
        boolean canDeal = gameState != BlackjackState.PLAYING || playerCards.isEmpty();

        if (canDeal) {
            drawBtn(g, x+W/2-35, aby, 70, 16, "DEAL", isHover(mx,my, x+W/2-35,aby,70,16) ? GOLD : WHITE, GREEN);
        } else {
            drawBtn(g, x+10,      aby, 60, 16, "HIT",    isHover(mx,my, x+10,aby,60,16)    ? GOLD : WHITE, GREEN);
            drawBtn(g, x+78,      aby, 60, 16, "STAND",  isHover(mx,my, x+78,aby,60,16)    ? GOLD : WHITE, ORANGE);
            drawBtn(g, x+146,     aby, 70, 16, "DOBLAR", isHover(mx,my, x+146,aby,70,16)   ? GOLD : WHITE, CYAN);
        }
    }

    private void renderHand(GuiGraphics g, int x, int y, List<Integer> cards, int visible) {
        for (int i = 0; i < cards.size(); i++) {
            int cx = x + i*18;
            boolean show = i < visible;
            g.fill(cx, y, cx+16, y+22, show ? 0xFFEEEEDD : DARK_BTN);
            g.fill(cx, y, cx+1,  y+22, GRAY);
            g.fill(cx+15, y, cx+16, y+22, GRAY);
            g.fill(cx, y,    cx+16, y+1,  GRAY);
            g.fill(cx, y+21, cx+16, y+22, GRAY);
            if (show) {
                int rank = cards.get(i);
                String lbl = rankLabel(rank);
                int col = 0xFF111111;
                g.drawString(font, lbl, cx+1, y+1, col, false);
            } else {
                g.drawString(font, "?", cx+4, y+7, GRAY, false);
            }
        }
    }

    // ─── Shared UI ─────────────────────────────────────────────────────────
    private void drawBetSelector(GuiGraphics g, int x, int y, int mx, int my) {
        g.drawString(font, "Apuesta:", x+8, y+3, GRAY, false);
        int bx = x + 60;
        for (int i = 0; i < BET_OPTIONS.length; i++) {
            int bw = font.width(String.valueOf(BET_OPTIONS[i])) + 8;
            boolean sel = i == selectedBet;
            boolean hov = isHover(mx,my, bx, y, bw, 14);
            int bg   = sel ? DARK_SEL : hov ? 0xFF1A1A3A : DARK_BTN;
            int border = sel ? GOLD : BTN_BORD;
            g.fill(bx, y, bx+bw, y+14, bg);
            g.fill(bx, y, bx+1, y+14, border);
            g.fill(bx+bw-1, y, bx+bw, y+14, border);
            g.fill(bx, y, bx+bw, y+1, border);
            g.fill(bx, y+13, bx+bw, y+14, border);
            g.drawString(font, String.valueOf(BET_OPTIONS[i]), bx+4, y+3,
                sel ? GOLD : WHITE, false);
            bx += bw + 3;
        }
    }

    private void drawBtn(GuiGraphics g, int x, int y, int w, int h, String label, int txtCol, int borderCol) {
        g.fill(x, y, x+w, y+h, DARK_BTN);
        g.fill(x, y, x+1, y+h, borderCol);
        g.fill(x+w-1, y, x+w, y+h, borderCol);
        g.fill(x, y, x+w, y+1, borderCol);
        g.fill(x, y+h-1, x+w, y+h, borderCol);
        g.drawString(font, label, x + w/2 - font.width(label)/2, y+(h-7)/2, txtCol, false);
    }

    private void drawSelectBtn(GuiGraphics g, int x, int y, int w, int h,
                                String label, boolean selected, int color, int mx, int my) {
        int bg = selected ? DARK_SEL : isHover(mx,my,x,y,w,h) ? 0xFF1A1A3A : DARK_BTN;
        g.fill(x, y, x+w, y+h, bg);
        g.fill(x, y, x+1, y+h, color);
        g.fill(x+w-1, y, x+w, y+h, color);
        g.fill(x, y, x+w, y+1, color);
        g.fill(x, y+h-1, x+w, y+h, color);
        g.drawString(font, label, x + w/2 - font.width(label)/2, y+(h-7)/2,
            selected ? GOLD : WHITE, false);
    }

    private static boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x+w && my >= y && my < y+h;
    }

    // ─── Mouse ─────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int x = leftPos, y = topPos;
        int imx = (int)mx, imy = (int)my;

        // Tab clicks
        int tw = W/3;
        for (int i = 0; i < 3; i++) {
            if (isHover(imx, imy, x + i*tw, y+20, tw, 16)) {
                activeTab = i;
                lastMsg = "";
                return true;
            }
        }

        // Bet selector
        int bsy = betSelectorY();
        int bx = x + 60;
        for (int i = 0; i < BET_OPTIONS.length; i++) {
            int bw = font.width(String.valueOf(BET_OPTIONS[i])) + 8;
            if (isHover(imx, imy, bx, y+38+bsy, bw, 14)) { selectedBet = i; return true; }
            bx += bw + 3;
        }

        // Tab-specific buttons
        int cy = y + 38;
        if (activeTab == TAB_SLOTS)     handleSlotsClick(imx, imy, x, cy);
        if (activeTab == TAB_ROULETTE)  handleRouletteClick(imx, imy, x, cy);
        if (activeTab == TAB_BLACKJACK) handleBlackjackClick(imx, imy, x, cy);

        return super.mouseClicked(mx, my, btn);
    }

    private int betSelectorY() {
        return switch (activeTab) {
            case TAB_SLOTS     -> 62;
            case TAB_ROULETTE  -> 90;  // wheel(36) + 3 bet rows(3×18) = 90
            case TAB_BLACKJACK -> 72;
            default            -> 62;
        };
    }

    private void handleSlotsClick(int mx, int my, int x, int y) {
        if (isHover(mx, my, x+W/2-40, y+100, 80, 16)) {
            PacketDistributor.sendToServer(new CasinoBetPayload(0, BET_OPTIONS[selectedBet], 0));
        }
    }

    private void handleRouletteClick(int mx, int my, int x, int y) {
        // Row 1: Red/Black
        int by1 = y + 36;
        int bw1 = W/2 - 20;
        if (isHover(mx,my, x+10, by1, bw1, 14))        selectedRoulBet = 0;
        if (isHover(mx,my, x+10+W/2-15, by1, bw1, 14)) selectedRoulBet = 1;

        // Row 2
        int by2 = by1 + 18;
        int bw2 = (W-20)/4 - 2;
        for (int i = 0; i < 4; i++)
            if (isHover(mx,my, x+10+i*(bw2+2), by2, bw2, 14)) selectedRoulBet = i+2;

        // Row 3
        int by3 = by2 + 18;
        int bw3 = (W-20)/3 - 2;
        for (int i = 0; i < 3; i++)
            if (isHover(mx,my, x+10+i*(bw3+2), by3, bw3, 14)) selectedRoulBet = i+6;

        // GIRAR
        if (isHover(mx,my, x+W/2-40, by3+36, 80, 16))
            PacketDistributor.sendToServer(new CasinoBetPayload(1, BET_OPTIONS[selectedBet], selectedRoulBet));
    }

    private void handleBlackjackClick(int mx, int my, int x, int y) {
        int aby = y + 90;
        boolean playing = bjData != null && bjData.length > 0 && bjData[0] == 0 // PLAYING
            && bjData.length > 3; // has player cards

        if (!playing) {
            if (isHover(mx,my, x+W/2-35, aby, 70, 16))
                PacketDistributor.sendToServer(new CasinoActionPayload(0, BET_OPTIONS[selectedBet]));
        } else {
            if (isHover(mx,my, x+10,  aby, 60, 16)) PacketDistributor.sendToServer(new CasinoActionPayload(1, 0));
            if (isHover(mx,my, x+78,  aby, 60, 16)) PacketDistributor.sendToServer(new CasinoActionPayload(2, 0));
            if (isHover(mx,my, x+146, aby, 70, 16)) PacketDistributor.sendToServer(new CasinoActionPayload(3, 0));
        }
    }

    @Override protected void renderBg(GuiGraphics g, float delta, int mx, int my) {}
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}

    // ─── Helpers ───────────────────────────────────────────────────────────
    private static boolean isRed(int n) {
        int[] reds = {1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};
        for (int r : reds) if (r == n) return true;
        return false;
    }

    private static int handValue(List<Integer> hand) {
        int v = 0, aces = 0;
        for (int c : hand) { v += Math.min(c, 10); if (c==1) aces++; }
        if (aces > 0 && v+10 <= 21) v += 10;
        return v;
    }

    private static int cardValue(int card) {
        return Math.min(card, 10) == 1 ? 11 : Math.min(card, 10);
    }

    private static String rankLabel(int rank) {
        return switch (rank) {
            case 1  -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> String.valueOf(rank);
        };
    }

}
