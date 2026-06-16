package com.mudaemod.mudaemod.network.handler;

import com.mudaemod.mudaemod.data.ActiveRoll;
import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.data.CharacterDatabase;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.network.CharacterResultPayload;
import com.mudaemod.mudaemod.network.ClaimRequestPayload;
import com.mudaemod.mudaemod.network.RollRequestPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class MudaeServerHandler {

    public static void handleRoll(RollRequestPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());

            if (!data.canRoll()) {
                long secs = data.getRollCooldownRemaining() / 1000;
                player.sendSystemMessage(Component.literal(
                    String.format("⏳ Sin rolls. Recarga en %dm %ds.", secs / 60, secs % 60)));
                return;
            }

            data.useRoll();
            MudaeDataManager.get().savePlayer(player.getUUID());

            // Roll from curated character database (guaranteed skin)
            CharacterDatabase.Entry entry = CharacterDatabase.rollRandom(payload.waifu());

            Character character = new Character(
                entry.id(), entry.name(), entry.animeName(), entry.skinUUID(), entry.kakeraValue());

            UUID rollKey = UUID.nameUUIDFromBytes(("roll_" + character.id()).getBytes());
            MudaeDataManager.get().setActiveRoll(rollKey, new ActiveRoll(character, player.getUUID()));

            var resultPayload = new CharacterResultPayload(
                character.id(), character.name(), character.animeName(),
                entry.skinUUID(), data.getKakera(), character.kakeraValue()
            );

            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                PacketDistributor.sendToPlayer(p, resultPayload);
            }

            player.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("🎲 ")
                    .append(Component.literal(player.getName().getString()).withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal(" invocó a ").withStyle(s -> s.withColor(0xFFFFFF)))
                    .append(Component.literal(character.name()).withStyle(s -> s.withColor(0xFFD700).withBold(true)))
                    .append(Component.literal(" de " + character.animeName() + " — ¡claimea con 💍!").withStyle(s -> s.withColor(0xADD8E6))),
                false
            );
        });
    }

    public static void handleClaim(ClaimRequestPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());

            if (!data.canClaim()) {
                player.sendSystemMessage(Component.literal("💔 Ya claimedaste recientemente. Esperá para el próximo."));
                return;
            }

            UUID rollKey = UUID.nameUUIDFromBytes(("roll_" + payload.characterId()).getBytes());
            ActiveRoll roll = MudaeDataManager.get().getActiveRoll(rollKey);

            if (roll == null || roll.isExpired()) {
                player.sendSystemMessage(Component.literal("❌ El personaje ya no está disponible (venció el tiempo)."));
                return;
            }

            if (data.hasCharacter(payload.characterId())) {
                player.sendSystemMessage(Component.literal("❌ Ya tenés a " + roll.character.name() + " en tu harem."));
                return;
            }

            data.claim(roll.character);
            MudaeDataManager.get().removeActiveRoll(rollKey);
            MudaeDataManager.get().savePlayer(player.getUUID());

            player.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("💍 ")
                    .append(Component.literal(player.getName().getString()).withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal(" se casó con ").withStyle(s -> s.withColor(0xFFFFFF)))
                    .append(Component.literal(roll.character.name()).withStyle(s -> s.withColor(0xFF69B4).withBold(true)))
                    .append(Component.literal(" de " + roll.character.animeName() + "!").withStyle(s -> s.withColor(0xADD8E6))),
                false
            );
        });
    }
}
