package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CharacterResultPayload(
    int id,
    String name,
    String animeName,
    String imageUrl,
    int playerKakera
) implements CustomPacketPayload {

    public static final Type<CharacterResultPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MudaeMod.MODID, "character_result"));

    public static final StreamCodec<ByteBuf, CharacterResultPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT,        CharacterResultPayload::id,
            ByteBufCodecs.STRING_UTF8, CharacterResultPayload::name,
            ByteBufCodecs.STRING_UTF8, CharacterResultPayload::animeName,
            ByteBufCodecs.STRING_UTF8, CharacterResultPayload::imageUrl,
            ByteBufCodecs.INT,        CharacterResultPayload::playerKakera,
            CharacterResultPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
