package com.mudaemod.mudaemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyStatPayload(int statIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BuyStatPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mudaemod", "buystat"));

    public static final StreamCodec<FriendlyByteBuf, BuyStatPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> buf.writeInt(p.statIndex()),
        buf -> new BuyStatPayload(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
