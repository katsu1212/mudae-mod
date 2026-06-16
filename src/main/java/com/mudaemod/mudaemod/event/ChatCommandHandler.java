package com.mudaemod.mudaemod.event;

import com.mudaemod.mudaemod.data.CharacterDatabase;
import com.mudaemod.mudaemod.data.GlobalMudaeData;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.UUID;

public class ChatCommandHandler {

    public static void onChat(ServerChatEvent event) {
        String msg = event.getMessage().getString().trim();
        String lower = msg.toLowerCase();

        boolean isRoll   = lower.equals("$w") || lower.equals("$waifu")
                        || lower.equals("$h") || lower.equals("$husbando")
                        || lower.equals("$m") || lower.equals("$mudae");
        boolean isClaim  = lower.equals("$claim");
        boolean isKakera = lower.equals("$kakera");
        boolean isDebug  = lower.equals("$debug");
        boolean isIm     = lower.startsWith("$im ");

        if (!isRoll && !isClaim && !isKakera && !isDebug && !isIm) return;

        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();

        if (isRoll)        handleRoll(player);
        else if (isClaim)  handleClaim(player);
        else if (isKakera) handleKakera(player);
        else if (isDebug)  handleDebug(player);
        else               handleIm(player, msg.substring(4).trim());
    }

    // ─── Roll ──────────────────────────────────────────────────────────────
    private static void handleRoll(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());

        if (!data.canRoll()) {
            long secs = data.getRollCooldownRemaining() / 1000;
            player.sendSystemMessage(Component.literal(
                String.format("Sin rolls. Recarga en %dm %ds. (16 rolls/hora)", secs / 60, secs % 60)));
            return;
        }

        GlobalMudaeData global = GlobalMudaeData.get(player.getServer());
        com.mudaemod.mudaemod.data.Character character =
            CharacterDatabase.rollRandomExcluding(global.getClaimedIds());

        if (character == null) {
            player.sendSystemMessage(Component.literal("El pool de personajes está vacío."));
            return;
        }

        data.useRoll();
        mgr.savePlayer(player.getUUID());
        global.setActiveRoll(character, player.getUUID());

        broadcastRoll(player.getServer(), player, character);
    }

    private static void broadcastRoll(MinecraftServer server, ServerPlayer player,
                                      com.mudaemod.mudaemod.data.Character c) {
        String pName = player.getName().getString();
        int rank = c.getRank();
        int ka   = c.kakeraValue();

        switch (rank) {
            case 5 -> {
                // ⭐ LEGENDARIO — anuncio dramático de 3 líneas
                broadcast(server, Component.literal("✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦")
                    .withStyle(s -> s.withColor(0xFFD700).withBold(true)));
                broadcast(server, Component.literal("  ⭐ ¡¡LEGENDARIO!! ⭐  ")
                    .withStyle(s -> s.withColor(0xFFD700).withBold(true))
                    .append(Component.literal(pName)
                        .withStyle(s -> s.withColor(0xFF8C00).withBold(true)))
                    .append(Component.literal(" invocó a ")
                        .withStyle(s -> s.withColor(0xFFEE55)))
                    .append(Component.literal(c.name())
                        .withStyle(s -> s.withColor(0xFFFFAA).withBold(true)))
                    .append(Component.literal(" ✦ " + c.animeName())
                        .withStyle(s -> s.withColor(0xFFD700))));
                broadcast(server, Component.literal("  💎 " + ka + " kakera  |  Top " + c.getId() + "  ←  $claim")
                    .withStyle(s -> s.withColor(0xFFAA00).withBold(true)));
                broadcast(server, Component.literal("✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦")
                    .withStyle(s -> s.withColor(0xFFD700).withBold(true)));
            }
            case 4 -> {
                // ✨ RARO — una línea destacada en violeta
                broadcast(server, Component.literal("✨ ¡RARO! ")
                    .withStyle(s -> s.withColor(0xCC44FF).withBold(true))
                    .append(Component.literal(pName)
                        .withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal(" invocó a ")
                        .withStyle(s -> s.withColor(0xFFFFFF)))
                    .append(Component.literal(c.name())
                        .withStyle(s -> s.withColor(0xFF77FF).withBold(true)))
                    .append(Component.literal(" de " + c.animeName())
                        .withStyle(s -> s.withColor(0xCC44FF)))
                    .append(Component.literal("  |  💎 " + ka + "  |  Top " + c.getId() + "  ←  $claim")
                        .withStyle(s -> s.withColor(0xAA22DD))));
            }
            case 3 -> {
                // 💫 POCO COMUN — cian, ligeramente diferente
                broadcast(server, Component.literal("💫 ")
                    .append(Component.literal(pName)
                        .withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal(" invocó a ")
                        .withStyle(s -> s.withColor(0xFFFFFF)))
                    .append(Component.literal(c.name())
                        .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                    .append(Component.literal(" de " + c.animeName())
                        .withStyle(s -> s.withColor(0x55DDDD)))
                    .append(Component.literal("  |  💎 " + ka + "  |  Top " + c.getId() + "  ←  $claim")
                        .withStyle(s -> s.withColor(0x44BBCC))));
            }
            default -> {
                // Rank 1-2: formato normal
                broadcast(server, Component.literal("🎲 ")
                    .append(Component.literal(pName)
                        .withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal(" invocó a ")
                        .withStyle(s -> s.withColor(0xFFFFFF)))
                    .append(Component.literal(c.name())
                        .withStyle(s -> s.withColor(0xFFD700).withBold(true)))
                    .append(Component.literal(" de " + c.animeName())
                        .withStyle(s -> s.withColor(0xADD8E6)))
                    .append(Component.literal("  |  💎 " + ka + "  |  Top " + c.getId() + "  ←  $claim")
                        .withStyle(s -> s.withColor(0xAA55FF))));
            }
        }
    }

    private static void broadcast(MinecraftServer server, MutableComponent msg) {
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    // ─── Claim ─────────────────────────────────────────────────────────────
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

        broadcast(player.getServer(), Component.literal("💍 ")
            .append(Component.literal(player.getName().getString())
                .withStyle(s -> s.withColor(0x00FF7F).withBold(true)))
            .append(Component.literal(" se casó con ")
                .withStyle(s -> s.withColor(0xFFFFFF)))
            .append(Component.literal(character.name())
                .withStyle(s -> s.withColor(0xFF69B4).withBold(true)))
            .append(Component.literal(" de " + character.animeName() + "!")
                .withStyle(s -> s.withColor(0xADD8E6))));
    }

    // ─── $im ───────────────────────────────────────────────────────────────
    private static void handleIm(ServerPlayer player, String raw) {
        // Soporta: $im "Zero Two"  y  $im Zero Two
        String query = raw.startsWith("\"") && raw.endsWith("\"") && raw.length() > 2
            ? raw.substring(1, raw.length() - 1)
            : raw;

        if (query.isBlank()) {
            player.sendSystemMessage(Component.literal("Uso: $im \"nombre del personaje\""));
            return;
        }

        com.mudaemod.mudaemod.data.Character c = CharacterDatabase.searchByName(query);
        if (c == null) {
            player.sendSystemMessage(Component.literal("❌ No se encontró ningún personaje con ese nombre.")
                .withStyle(s -> s.withColor(0xFF5555)));
            return;
        }

        // Línea 1: nombre y serie
        int rankColor = switch (c.getRank()) {
            case 5 -> 0xFFD700;
            case 4 -> 0xCC44FF;
            case 3 -> 0x55FFFF;
            case 2 -> 0xADD8E6;
            default -> 0xAAAAAA;
        };
        player.sendSystemMessage(Component.literal("─────────────────────────")
            .withStyle(s -> s.withColor(rankColor)));
        player.sendSystemMessage(Component.literal("  " + c.name())
            .withStyle(s -> s.withColor(rankColor).withBold(true))
            .append(Component.literal("  —  " + c.animeName())
                .withStyle(s -> s.withColor(0xCCCCCC).withBold(false))));
        player.sendSystemMessage(Component.literal("  💎 " + c.kakeraValue() + " kakera   |   Top " + c.getId())
            .withStyle(s -> s.withColor(rankColor)));

        // Estado: ¿reclamado?
        GlobalMudaeData global = GlobalMudaeData.get(player.getServer());
        if (global.getClaimedIds().contains(c.getId())) {
            UUID ownerUuid = MudaeDataManager.get().findOwnerOf(c.getId());
            String ownerName = "alguien";
            if (ownerUuid != null) {
                ServerPlayer online = player.getServer().getPlayerList().getPlayer(ownerUuid);
                ownerName = online != null
                    ? online.getName().getString()
                    : ownerUuid.toString().substring(0, 8) + "… (offline)";
            }
            player.sendSystemMessage(Component.literal("  💍 Reclamado por " + ownerName)
                .withStyle(s -> s.withColor(0xFF69B4)));
        } else {
            player.sendSystemMessage(Component.literal("  ✅ Libre — nadie lo ha reclamado todavía")
                .withStyle(s -> s.withColor(0x55FF55)));
        }
        player.sendSystemMessage(Component.literal("─────────────────────────")
            .withStyle(s -> s.withColor(rankColor)));
    }

    // ─── Debug / Kakera ────────────────────────────────────────────────────
    private static void handleDebug(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());
        data.resetCooldowns();
        data.addKakera(99999);
        mgr.savePlayer(player.getUUID());
        player.sendSystemMessage(Component.literal("[DEBUG] Rolls y claim reseteados. +99999 kakera."));
    }

    private static void handleKakera(ServerPlayer player) {
        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());
        player.sendSystemMessage(Component.literal(
            "💎 Tenés " + data.getKakera() + " kakera. Rolls disponibles: " + data.getRollsRemaining() + "/16."));
    }
}
