package com.mudaemod.mudaemod.network.handler;

import com.mudaemod.mudaemod.data.*;
import com.mudaemod.mudaemod.network.BuyStatPayload;
import com.mudaemod.mudaemod.network.HaremPayload;
import com.mudaemod.mudaemod.network.SellPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.stream.Collectors;

public class MudaeServerHandler {

    public static void handleSell(SellPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            MudaeDataManager mgr = MudaeDataManager.get();
            PlayerData data = mgr.getPlayer(player.getUUID());

            Character toSell = data.getHarem().stream()
                .filter(c -> c.id() == payload.characterId())
                .findFirst().orElse(null);

            if (toSell == null) {
                player.sendSystemMessage(Component.literal("❌ No tenés ese personaje."));
                return;
            }

            data.removeFromHarem(toSell.id());
            data.addKakera(toSell.kakeraValue());
            GlobalMudaeData.get(player.getServer()).unclaimCharacter(toSell.id());
            mgr.savePlayer(player.getUUID());

            player.sendSystemMessage(Component.literal(
                "💰 Vendiste a " + toSell.name() + " por 💎 " + toSell.kakeraValue() + " kakera. Vuelve al pool."));

            buildAndSendHarem(player);
        });
    }

    public static void handleBuyStat(BuyStatPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            int idx = payload.statIndex();
            if (idx < 0 || idx >= 4) return;

            MudaeDataManager mgr = MudaeDataManager.get();
            PlayerData data = mgr.getPlayer(player.getUUID());

            if (data.getStatLevels()[idx] >= PlayerData.STAT_MAX) {
                player.sendSystemMessage(Component.literal("❌ " + PlayerData.STAT_NAMES[idx] + " ya está al máximo (5/5)."));
                return;
            }
            if (data.getKakera() < PlayerData.STAT_COSTS[idx]) {
                player.sendSystemMessage(Component.literal(
                    "❌ Kakera insuficiente. Necesitás 💎 " + PlayerData.STAT_COSTS[idx] + "."));
                return;
            }

            data.upgradeStat(idx);
            mgr.savePlayer(player.getUUID());
            applyStats(player, data);

            player.sendSystemMessage(Component.literal(
                "✨ " + PlayerData.STAT_NAMES[idx] + " subió a nivel " + data.getStatLevels()[idx] + "/5!"));

            buildAndSendHarem(player);
        });
    }

    public static void buildAndSendHarem(ServerPlayer player) {
        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());
        List<HaremPayload.HaremEntry> entries = data.getHarem().stream()
            .map(c -> new HaremPayload.HaremEntry(c.id(), c.name(), c.animeName(), c.kakeraValue()))
            .collect(Collectors.toList());
        int[] stats = data.getStatLevels();
        PacketDistributor.sendToPlayer(player,
            new HaremPayload(entries, data.getKakera(), stats[0], stats[1], stats[2], stats[3]));
    }

    public static void applyStats(ServerPlayer player, PlayerData data) {
        int[] lvls = data.getStatLevels();

        applyModifier(player, Attributes.MAX_HEALTH,
            ResourceLocation.fromNamespaceAndPath("mudaemod", "vida"),
            lvls[0] * 4.0, AttributeModifier.Operation.ADD_VALUE);

        applyModifier(player, Attributes.MOVEMENT_SPEED,
            ResourceLocation.fromNamespaceAndPath("mudaemod", "velocidad"),
            lvls[1] * 0.02, AttributeModifier.Operation.ADD_VALUE);

        applyModifier(player, Attributes.ATTACK_DAMAGE,
            ResourceLocation.fromNamespaceAndPath("mudaemod", "fuerza"),
            lvls[2] * 2.0, AttributeModifier.Operation.ADD_VALUE);

        applyModifier(player, Attributes.ARMOR,
            ResourceLocation.fromNamespaceAndPath("mudaemod", "defensa"),
            lvls[3] * 2.0, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void applyModifier(ServerPlayer player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
            ResourceLocation id, double amount, AttributeModifier.Operation op) {
        var instance = player.getAttribute(attr);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount > 0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, op));
        }
    }
}
