package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClaimRequestPayload(int characterId) implements CustomPacketPayload {

    public static final Type<ClaimRequestPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "claim_request"));

    public static final StreamCodec<ByteBuf, ClaimRequestPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, ClaimRequestPayload::characterId,
            ClaimRequestPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
