package com.mudaemod.mudaemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → Server: place a bet on slots or roulette. */
public record CasinoBetPayload(int gameType, int betAmount, int betOption) implements CustomPacketPayload {

    // gameType: 0=slots, 1=roulette
    // betOption (roulette): 0=Red, 1=Black, 2=Even, 3=Odd, 4=Low, 5=High, 6=Doc1, 7=Doc2, 8=Doc3

    public static final CustomPacketPayload.Type<CasinoBetPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mudaemod", "casino_bet"));

    public static final StreamCodec<FriendlyByteBuf, CasinoBetPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> { buf.writeInt(p.gameType()); buf.writeInt(p.betAmount()); buf.writeInt(p.betOption()); },
        buf -> new CasinoBetPayload(buf.readInt(), buf.readInt(), buf.readInt())
    );

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
