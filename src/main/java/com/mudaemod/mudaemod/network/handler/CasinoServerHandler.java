package com.mudaemod.mudaemod.network.handler;

import com.mudaemod.mudaemod.data.BlackjackState;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.network.CasinoActionPayload;
import com.mudaemod.mudaemod.network.CasinoBetPayload;
import com.mudaemod.mudaemod.network.CasinoResultPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CasinoServerHandler {

    private static final Random RNG = new Random();
    private static final Map<UUID, BlackjackState> BJ_GAMES = new HashMap<>();

    // ─── Slots & Roulette ─────────────────────────────────────────────────
    public static void handleBet(CasinoBetPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            MudaeDataManager mgr = MudaeDataManager.get();
            PlayerData data = mgr.getPlayer(player.getUUID());

            int bet = payload.betAmount();
            if (bet <= 0 || data.getKakera() < bet) {
                send(player, new CasinoResultPayload(payload.gameType(), new int[0], 0,
                    data.getKakera(), "❌ Kakera insuficiente."));
                return;
            }

            if (payload.gameType() == 0) handleSlots(player, data, mgr, bet);
            else                          handleRoulette(player, data, mgr, bet, payload.betOption());
        });
    }

    private static void handleSlots(ServerPlayer player, PlayerData data, MudaeDataManager mgr, int bet) {
        // 6 symbols: 0=BAR 1=LIM 2=NAR 3=STAR 4=GEM 5=777
        // Weighted: BAR 30%, LIM 25%, NAR 20%, STAR 14%, GEM 8%, 777 3%
        int[] weights = {30, 25, 20, 14, 8, 3};
        int[] reels = new int[3];
        for (int i = 0; i < 3; i++) {
            int r = RNG.nextInt(100);
            int acc = 0;
            for (int s = 0; s < weights.length; s++) {
                acc += weights[s];
                if (r < acc) { reels[i] = s; break; }
            }
        }

        int mult = 0;
        if (reels[0] == reels[1] && reels[1] == reels[2]) {
            mult = switch (reels[0]) {
                case 5 -> 50;  // 777
                case 4 -> 20;  // GEM
                case 3 -> 10;  // STAR
                default -> 3;  // fruit
            };
        } else if (reels[0] == reels[1] || reels[1] == reels[2] || reels[0] == reels[2]) {
            mult = 1; // par: devuelve la apuesta
        }

        int winDelta;
        String msg;
        if (mult == 0) {
            winDelta = -bet;
            msg = "Perdiste 💎 " + bet;
        } else if (mult == 1) {
            winDelta = 0;
            msg = "Par — recuperaste tu apuesta";
        } else {
            winDelta = bet * (mult - 1);
            msg = "¡Ganaste! x" + mult + " → +💎 " + winDelta;
        }

        data.addKakera(winDelta);
        mgr.savePlayer(player.getUUID());
        send(player, new CasinoResultPayload(0, reels, winDelta, data.getKakera(), msg));
    }

    private static void handleRoulette(ServerPlayer player, PlayerData data, MudaeDataManager mgr,
                                        int bet, int betOption) {
        int spin = RNG.nextInt(37); // 0-36
        boolean isRed = isRed(spin);
        boolean isZero = spin == 0;

        boolean won = false;
        int mult = 2;
        switch (betOption) {
            case 0 -> won = !isZero && isRed;          // Red
            case 1 -> won = !isZero && !isRed;         // Black
            case 2 -> won = !isZero && spin % 2 == 0;  // Even
            case 3 -> won = !isZero && spin % 2 != 0;  // Odd
            case 4 -> won = spin >= 1 && spin <= 18;   // Low
            case 5 -> won = spin >= 19 && spin <= 36;  // High
            case 6 -> { won = spin >= 1 && spin <= 12;  mult = 3; } // Docena 1
            case 7 -> { won = spin >= 13 && spin <= 24; mult = 3; } // Docena 2
            case 8 -> { won = spin >= 25 && spin <= 36; mult = 3; } // Docena 3
        }

        int winDelta;
        String msg;
        if (won) {
            winDelta = bet * (mult - 1);
            msg = "Salió " + spin + (isZero ? " 🟢" : isRed ? " 🔴" : " ⚫") + " — ¡Ganaste! +💎 " + winDelta;
        } else {
            winDelta = -bet;
            msg = "Salió " + spin + (isZero ? " 🟢" : isRed ? " 🔴" : " ⚫") + " — Perdiste 💎 " + bet;
        }

        data.addKakera(winDelta);
        mgr.savePlayer(player.getUUID());
        send(player, new CasinoResultPayload(1, new int[]{spin}, winDelta, data.getKakera(), msg));
    }

    private static boolean isRed(int n) {
        int[] reds = {1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};
        for (int r : reds) if (r == n) return true;
        return false;
    }

    // ─── Blackjack ────────────────────────────────────────────────────────
    public static void handleAction(CasinoActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            MudaeDataManager mgr = MudaeDataManager.get();
            PlayerData data = mgr.getPlayer(player.getUUID());
            UUID uuid = player.getUUID();

            switch (payload.action()) {
                case 0 -> bjDeal(player, data, mgr, uuid, payload.betAmount());
                case 1 -> bjHit(player, data, mgr, uuid);
                case 2 -> bjStand(player, data, mgr, uuid);
                case 3 -> bjDouble(player, data, mgr, uuid);
            }
        });
    }

    private static void bjDeal(ServerPlayer player, PlayerData data, MudaeDataManager mgr,
                                UUID uuid, int bet) {
        if (bet <= 0 || data.getKakera() < bet) {
            send(player, new CasinoResultPayload(2, new int[0], 0, data.getKakera(), "❌ Kakera insuficiente."));
            return;
        }
        data.addKakera(-bet);
        mgr.savePlayer(player.getUUID());

        BlackjackState bj = BlackjackState.deal(bet);
        BJ_GAMES.put(uuid, bj);

        // Check immediate blackjack
        if (bj.isBlackjack(bj.playerHand)) {
            bj.dealerRevealed = true;
            int winDelta;
            String msg;
            if (bj.isBlackjack(bj.dealerHand)) {
                // Both blackjack → push
                data.addKakera(bet);
                winDelta = 0;
                msg = "¡Ambos tienen Blackjack! — Empate";
            } else {
                winDelta = (int)(bet * 1.5);
                data.addKakera(bet + winDelta);
                msg = "¡¡BLACKJACK!! +💎 " + winDelta;
            }
            mgr.savePlayer(player.getUUID());
            BJ_GAMES.remove(uuid);
            send(player, new CasinoResultPayload(2, bj.encode(BlackjackState.BLACKJACK),
                winDelta, data.getKakera(), msg));
            return;
        }

        send(player, new CasinoResultPayload(2, bj.encode(BlackjackState.PLAYING),
            -bet, data.getKakera(), "Tu turno — HIT, STAND o DOBLAR"));
    }

    private static void bjHit(ServerPlayer player, PlayerData data, MudaeDataManager mgr, UUID uuid) {
        BlackjackState bj = BJ_GAMES.get(uuid);
        if (bj == null) { send(player, noGame(data)); return; }

        bj.playerHand.add(bj.draw());
        int pv = bj.playerValue();

        if (pv > 21) {
            // Bust
            BJ_GAMES.remove(uuid);
            send(player, new CasinoResultPayload(2, bj.encode(BlackjackState.PLAYER_BUST),
                0, data.getKakera(), "¡Te pasaste! (" + pv + ") — Perdiste 💎 " + bj.betAmount));
        } else {
            send(player, new CasinoResultPayload(2, bj.encode(BlackjackState.PLAYING),
                0, data.getKakera(), "Total: " + pv + " — HIT, STAND o DOBLAR"));
        }
    }

    private static void bjStand(ServerPlayer player, PlayerData data, MudaeDataManager mgr, UUID uuid) {
        BlackjackState bj = BJ_GAMES.get(uuid);
        if (bj == null) { send(player, noGame(data)); return; }

        bj.dealerPlay();
        int pv = bj.playerValue();
        int dv = bj.dealerValue();
        BJ_GAMES.remove(uuid);

        int gameState;
        int winDelta;
        String msg;

        if (dv > 21) {
            gameState = BlackjackState.DEALER_BUST;
            winDelta = bj.betAmount;
            msg = "Dealer se pasó (" + dv + ") — ¡Ganaste! +💎 " + winDelta;
            data.addKakera(bj.betAmount + winDelta);
        } else if (pv > dv) {
            gameState = BlackjackState.PLAYER_WIN;
            winDelta = bj.betAmount;
            msg = "¡Ganaste! " + pv + " vs " + dv + " — +💎 " + winDelta;
            data.addKakera(bj.betAmount + winDelta);
        } else if (dv > pv) {
            gameState = BlackjackState.DEALER_WIN;
            winDelta = 0;
            msg = "Dealer gana " + dv + " vs " + pv + " — Perdiste 💎 " + bj.betAmount;
        } else {
            gameState = BlackjackState.PUSH;
            winDelta = 0;
            msg = "Empate (" + pv + ") — Recuperaste tu apuesta";
            data.addKakera(bj.betAmount);
        }
        mgr.savePlayer(player.getUUID());
        send(player, new CasinoResultPayload(2, bj.encode(gameState), winDelta, data.getKakera(), msg));
    }

    private static void bjDouble(ServerPlayer player, PlayerData data, MudaeDataManager mgr, UUID uuid) {
        BlackjackState bj = BJ_GAMES.get(uuid);
        if (bj == null) { send(player, noGame(data)); return; }

        if (data.getKakera() < bj.betAmount) {
            send(player, new CasinoResultPayload(2, bj.encode(BlackjackState.PLAYING),
                0, data.getKakera(), "❌ No tenés kakera para doblar."));
            return;
        }
        data.addKakera(-bj.betAmount);
        bj.betAmount *= 2;
        mgr.savePlayer(player.getUUID());

        bj.playerHand.add(bj.draw());
        int pv = bj.playerValue();

        if (pv > 21) {
            BJ_GAMES.remove(uuid);
            send(player, new CasinoResultPayload(2, bj.encode(BlackjackState.PLAYER_BUST),
                0, data.getKakera(), "¡Te pasaste! (" + pv + ") — Perdiste 💎 " + bj.betAmount));
        } else {
            // Auto-stand after double
            bjStand(player, data, mgr, uuid);
        }
    }

    private static CasinoResultPayload noGame(PlayerData data) {
        return new CasinoResultPayload(2, new int[0], 0, data.getKakera(),
            "No hay partida activa. Presioná DEAL para empezar.");
    }

    private static void send(ServerPlayer player, CasinoResultPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
