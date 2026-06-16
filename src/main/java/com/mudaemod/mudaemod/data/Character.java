package com.mudaemod.mudaemod.data;

public record Character(int id, String name, String animeName, String imageUrl) {

    public int kakeraValue() {
        // Simple deterministic value based on id so the same character always gives same kakera
        return 20 + (id % 80);
    }
}
