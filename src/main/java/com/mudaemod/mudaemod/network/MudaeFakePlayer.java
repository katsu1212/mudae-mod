package com.mudaemod.mudaemod.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MudaeFakePlayer extends RemotePlayer {

    private final ResourceLocation skinTexture;

    public MudaeFakePlayer(ClientLevel level, GameProfile profile, ResourceLocation skinTexture) {
        super(level, profile);
        this.skinTexture = skinTexture;
    }

    @Override
    public ResourceLocation getSkinTextureLocation() {
        return skinTexture;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return true;
    }
}
