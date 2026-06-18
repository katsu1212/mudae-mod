package com.mudaemod.mudaemod.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackjackState {

    public List<Integer> playerHand = new ArrayList<>();
    public List<Integer> dealerHand = new ArrayList<>();
    public List<Integer> deck       = new ArrayList<>();
    public int betAmount;
    public boolean dealerRevealed = false;

    public static BlackjackState deal(int bet) {
        BlackjackState s = new BlackjackState();
        s.betAmount = bet;
        // Build and shuffle 6-deck shoe
        for (int d = 0; d < 6; d++)
            for (int suit = 0; suit < 4; suit++)
                for (int rank = 1; rank <= 13; rank++)
                    s.deck.add(rank);
        Collections.shuffle(s.deck);
        s.playerHand.add(s.draw());
        s.dealerHand.add(s.draw());
        s.playerHand.add(s.draw());
        s.dealerHand.add(s.draw());
        return s;
    }

    public int draw() {
        if (deck.isEmpty()) return 1;
        return deck.remove(deck.size() - 1);
    }

    public static int handValue(List<Integer> hand) {
        int value = 0;
        int aces = 0;
        for (int card : hand) {
            int v = Math.min(card, 10);
            value += v;
            if (card == 1) aces++;
        }
        // Promote one ace to 11 if it doesn't bust
        if (aces > 0 && value + 10 <= 21) value += 10;
        return value;
    }

    public boolean isBlackjack(List<Integer> hand) {
        if (hand.size() != 2) return false;
        int a = hand.get(0), b = hand.get(1);
        return (a == 1 && b >= 10) || (b == 1 && a >= 10);
    }

    public int playerValue() { return handValue(playerHand); }
    public int dealerValue() { return handValue(dealerHand); }

    /** Dealer plays out: hits until 17+. */
    public void dealerPlay() {
        dealerRevealed = true;
        while (dealerValue() < 17) dealerHand.add(draw());
    }

    /** Encode state for network: [gameState, dealerRevealed? 1:0, d1,d2..., -1, p1,p2...] */
    public int[] encode(int gameState) {
        List<Integer> out = new ArrayList<>();
        out.add(gameState);
        out.add(dealerRevealed ? 1 : 0);
        out.addAll(dealerHand);
        out.add(-1);
        out.addAll(playerHand);
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    // gameState constants
    public static final int PLAYING    = 0;
    public static final int PLAYER_WIN = 1;
    public static final int DEALER_WIN = 2;
    public static final int PUSH       = 3;
    public static final int BLACKJACK  = 4;
    public static final int PLAYER_BUST = 5;
    public static final int DEALER_BUST = 6;
}
