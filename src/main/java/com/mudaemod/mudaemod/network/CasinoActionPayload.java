package com.mudaemod.mudaemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → Server: blackjack action. */
public record CasinoActionPayload(int action, int betAmount) implements CustomPacketPayload {

    // action: 0=DEAL, 1=HIT, 2=STAND, 3=DOUBLE
    // betAmount only used on DEAL

    public static final CustomPacketPayload.Type<CasinoActionPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mudaemod", "casino_action"));

    public static final StreamCodec<FriendlyByteBuf, CasinoActionPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> { buf.writeInt(p.action()); buf.writeInt(p.betAmount()); },
        buf -> new CasinoActionPayload(buf.readInt(), buf.readInt())
    );

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
