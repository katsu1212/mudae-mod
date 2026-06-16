package com.mudaemod.mudaemod.data;

public record Character(int id, String name, String animeName, int rank, boolean waifu, int kakera) {

    public int getId()       { return id; }
    public int getRank()     { return rank; }
    public boolean isWaifu() { return waifu; }
    public int kakeraValue() { return kakera; }
}
