package com.mudaemod.mudaemod.data;

public record Character(int id, String name, String animeName, int rank, boolean waifu) {

    public int getId()       { return id; }
    public int getRank()     { return rank; }
    public boolean isWaifu() { return waifu; }

    /** Valor en kakera derivado del rank del personaje. */
    public int kakeraValue() {
        return switch (rank) {
            case 1  -> 50;
            case 2  -> 150;
            case 3  -> 400;
            case 4  -> 900;
            default -> 2000;
        };
    }
}
