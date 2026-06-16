package com.mudaemod.mudaemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SellPayload(int characterId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SellPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mudaemod", "sell"));

    public static final StreamCodec<FriendlyByteBuf, SellPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> buf.writeInt(p.characterId()),
        buf -> new SellPayload(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
