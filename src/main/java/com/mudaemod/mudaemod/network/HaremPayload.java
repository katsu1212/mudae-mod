package com.mudaemod.mudaemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record HaremPayload(List<HaremEntry> entries, int kakera, int statVida, int statVel, int statFuerza, int statDef) implements CustomPacketPayload {

    public record HaremEntry(int id, String name, String animeName, int kakeraValue) {}

    public static final CustomPacketPayload.Type<HaremPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mudaemod", "harem"));

    private static final StreamCodec<FriendlyByteBuf, HaremEntry> ENTRY_CODEC = StreamCodec.of(
        (buf, e) -> {
            buf.writeInt(e.id());
            buf.writeUtf(e.name());
            buf.writeUtf(e.animeName());
            buf.writeInt(e.kakeraValue());
        },
        buf -> new HaremEntry(buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readInt())
    );

    public static final StreamCodec<FriendlyByteBuf, HaremPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> {
            buf.writeInt(p.entries().size());
            for (HaremEntry e : p.entries()) ENTRY_CODEC.encode(buf, e);
            buf.writeInt(p.kakera());
            buf.writeInt(p.statVida());
            buf.writeInt(p.statVel());
            buf.writeInt(p.statFuerza());
            buf.writeInt(p.statDef());
        },
        buf -> {
            int size = buf.readInt();
            List<HaremEntry> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) entries.add(ENTRY_CODEC.decode(buf));
            return new HaremPayload(entries, buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
        }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
