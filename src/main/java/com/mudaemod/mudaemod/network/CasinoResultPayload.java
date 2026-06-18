package com.mudaemod.mudaemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server → Client: result of a casino action. */
public record CasinoResultPayload(int gameType, int[] data, int winDelta, int newKakera, String msg)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CasinoResultPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mudaemod", "casino_result"));

    public static final StreamCodec<FriendlyByteBuf, CasinoResultPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> {
            buf.writeInt(p.gameType());
            buf.writeInt(p.data().length);
            for (int d : p.data()) buf.writeInt(d);
            buf.writeInt(p.winDelta());
            buf.writeInt(p.newKakera());
            buf.writeUtf(p.msg());
        },
        buf -> {
            int gameType = buf.readInt();
            int len = buf.readInt();
            int[] data = new int[len];
            for (int i = 0; i < len; i++) data[i] = buf.readInt();
            int winDelta = buf.readInt();
            int newKakera = buf.readInt();
            String msg = buf.readUtf();
            return new CasinoResultPayload(gameType, data, winDelta, newKakera, msg);
        }
    );

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
