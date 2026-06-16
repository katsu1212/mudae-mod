package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RollRequestPayload(boolean waifu) implements CustomPacketPayload {

    public static final Type<RollRequestPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "roll_request"));

    public static final StreamCodec<ByteBuf, RollRequestPayload> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.BOOL, RollRequestPayload::waifu, RollRequestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
