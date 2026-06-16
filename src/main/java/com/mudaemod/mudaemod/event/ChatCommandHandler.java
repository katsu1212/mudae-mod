package com.mudaemod.mudaemod.event;

import com.mudaemod.mudaemod.data.CharacterDatabase;
import com.mudaemod.mudaemod.data.GlobalMudaeData;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;

public class ChatCommandHandler {

    public static void onChat(ServerChatEvent event) {
        String msg = event.getMessage().getString().trim();
        String lower = msg.toLowerCase();

        if (!lower.equals("$w") && !lower.equals("$waifu")
            && !lower.equals("$h") && !lower.equals("$husbando")
            && !lower.equals("$m") && !lower.equals("$mudae")
            && !lower.equals("$claim") && !lower.equals("$kakera")
            && !lower.equals("$debug")) {
            return;
        }

        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();

        switch (lower) {
            case "$w", "$waifu", "$h", "$husbando", "$m", "$mudae" -> handleRoll(player);
            case "$claim"  -> handleClaim(player);
            case "$kakera" -> handleKakera(player);
            case "$debug"  -> handleDebug(player);
        }
    }

    private static void handleRoll(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());

        if (!data.canRoll()) {
            long secs = data.getRollCooldownRemaining() / 1000;
            player.sendSystemMessage(Component.literal(
                String.format("Sin rolls. Recarga en %dm %ds. (16 rolls/hora)",
                    secs / 60, secs % 60)));
            return;
        }

        GlobalMudaeData global = GlobalMudaeData.get(player.getServer());
        com.mudaemod.mudaemod.data.Character character = CharacterDatabase.rollRandomExcluding(global.getClaimedIds());

        if (character == null) {
            player.sendSystemMessage(Component.literal("El pool de personajes está vacío."));
            return;
        }

        data.useRoll();
        mgr.savePlayer(player.getUUID());

        global.setActiveRoll(character, player.getUUID());

        player.getServer().getPlayerList().broadcastSystemMessage(
            Component.literal("🎲 ")
                .append(Component.literal(player.getName().getString())
                    .withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                .append(Component.literal(" invocó a ")
                    .withStyle(s -> s.withColor(0xFFFFFF)))
                .append(Component.literal(character.name())
                    .withStyle(s -> s.withColor(0xFFD700).withBold(true)))
                .append(Component.literal(" de " + character.animeName())
                    .withStyle(s -> s.withColor(0xADD8E6)))
                .append(Component.literal(" | 💎 " + character.kakeraValue() + " | Rank " + character.rank() + " ← $claim")
                    .withStyle(s -> s.withColor(0xAA55FF))),
            false
        );
    }

    private static void handleClaim(ServerPlayer player) {
        GlobalMudaeData global = GlobalMudaeData.get(player.getServer());

        if (!global.hasActiveRoll()) {
            player.sendSystemMessage(Component.literal("❌ No hay ningún personaje disponible para claimear."));
            return;
        }

        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());

        if (!data.canClaim()) {
            long secs = data.getClaimCooldownRemaining() / 1000;
            player.sendSystemMessage(Component.literal(
                String.format("💔 Claim en cooldown. Disponible en %dh %dm.", secs / 3600, (secs % 3600) / 60)));
            return;
        }

        if (data.hasCharacter(global.getActiveCharId())) {
            player.sendSystemMessage(Component.literal("❌ Ya tenés a " + global.getActiveCharName() + " en tu harem."));
            return;
        }

        int charId = global.getActiveCharId();
        com.mudaemod.mudaemod.data.Character character = CharacterDatabase.getById(charId);
        if (character == null) {
            player.sendSystemMessage(Component.literal("Error: personaje no encontrado en la base de datos."));
            return;
        }

        data.claim(character);
        global.claimCharacter(character.id());
        global.clearActiveRoll();
        mgr.savePlayer(player.getUUID());

        player.getServer().getPlayerList().broadcastSystemMessage(
            Component.literal("💍 ")
                .append(Component.literal(player.getName().getString())
                    .withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                .append(Component.literal(" se casó con ")
                    .withStyle(s -> s.withColor(0xFFFFFF)))
                .append(Component.literal(character.name())
                    .withStyle(s -> s.withColor(0xFF69B4).withBold(true)))
                .append(Component.literal(" de " + character.animeName() + "!")
                    .withStyle(s -> s.withColor(0xADD8E6))),
            false
        );
    }

    private static void handleDebug(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());
        data.resetCooldowns();
        data.addKakera(99999);
        mgr.savePlayer(player.getUUID());
        player.sendSystemMessage(Component.literal(
            "[DEBUG] Rolls y claim reseteados. +99999 kakera."));
    }

    private static void handleKakera(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());
        player.sendSystemMessage(Component.literal(
            "💎 Tenés " + data.getKakera() + " kakera. Rolls disponibles: " + data.getRollsRemaining() + "/16."));
    }
}
