package com.mudaemod.mudaemod.data;

public record Character(int id, String name, String animeName, String imageUrl) {

    public String displayName() {
        return name + " ❋ " + animeName;
    }
}
