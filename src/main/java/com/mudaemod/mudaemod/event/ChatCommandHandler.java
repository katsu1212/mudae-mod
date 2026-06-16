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
            && !lower.equals("$claim") && !lower.equals("$kakera")) {
            return;
        }

        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();

        switch (lower) {
            case "$w", "$waifu"      -> handleRoll(player, true);
            case "$h", "$husbando"   -> handleRoll(player, false);
            case "$claim"            -> handleClaim(player);
            case "$kakera"           -> handleKakera(player);
        }
    }

    private static void handleRoll(ServerPlayer player, boolean waifu) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());

        if (!data.canRoll()) {
            long secs = data.getRollCooldownRemaining() / 1000;
            player.sendSystemMessage(Component.literal(
                String.format("⏳ Sin rolls. Recarga en %dm %ds. (%d rolls/hora)",
                    secs / 60, secs % 60, PlayerData.STAT_MAX)));
            return;
        }

        GlobalMudaeData global = GlobalMudaeData.get(player.getServer());
        CharacterDatabase.Entry entry = CharacterDatabase.rollRandomExcluding(waifu, global.getClaimedIds());
        com.mudaemod.mudaemod.data.Character character = new com.mudaemod.mudaemod.data.Character(entry.id(), entry.name(), entry.animeName(), entry.skinUUID(), entry.kakeraValue());

        data.useRoll();
        mgr.savePlayer(player.getUUID());

        global.setActiveRoll(character, player.getUUID());
        int rank = CharacterDatabase.getRank(entry.id());

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
                .append(Component.literal(" | 💎 " + character.kakeraValue() + " | 🏆 #" + rank + " ← $claim")
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

        com.mudaemod.mudaemod.data.Character character = new com.mudaemod.mudaemod.data.Character(
            global.getActiveCharId(),
            global.getActiveCharName(),
            global.getActiveCharAnime(),
            "",
            global.getActiveKakera()
        );

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

    private static void handleKakera(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());
        player.sendSystemMessage(Component.literal(
            "💎 Tenés " + data.getKakera() + " kakera. Rolls disponibles: " + data.getRollsRemaining() + "/16."));
    }
}
