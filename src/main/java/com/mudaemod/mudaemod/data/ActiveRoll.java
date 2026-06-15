package com.mudaemod.mudaemod.data;

import java.util.UUID;

public class ActiveRoll {
    public final Character character;
    public final UUID rolledBy;
    public final long rolledAt;
    // Tiempo en ms para poder claimear (3 minutos)
    public static final long CLAIM_WINDOW_MS = 3 * 60 * 1000L;

    public ActiveRoll(Character character, UUID rolledBy) {
        this.character = character;
        this.rolledBy = rolledBy;
        this.rolledAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - rolledAt > CLAIM_WINDOW_MS;
    }
}
