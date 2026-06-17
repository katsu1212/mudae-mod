package com.mudaemod.mudaemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.data.CharacterDatabase;
import com.mudaemod.mudaemod.data.GlobalMudaeData;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class MudaeCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("harem")
            .executes(MudaeCommands::showHarem));

        dispatcher.register(Commands.literal("kakera")
            .executes(MudaeCommands::showKakera));

        dispatcher.register(Commands.literal("mudaeclaim")
            .then(Commands.argument("charId", IntegerArgumentType.integer(1))
                .executes(MudaeCommands::doClaim)));
    }

    // ─── /mudaeclaim <charId> ──────────────────────────────────────────────
    private static int doClaim(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int charId = IntegerArgumentType.getInteger(ctx, "charId");
        GlobalMudaeData global = GlobalMudaeData.get(player.getServer());
        GlobalMudaeData.PendingRoll roll = global.getPendingRoll(charId);

        if (roll == null) {
            player.sendSystemMessage(Component.literal(
                "❌ Ese personaje ya no está disponible (expiró o fue reclamado).")
                .withStyle(s -> s.withColor(0xFF5555)));
            return 0;
        }

        MudaeDataManager mgr = MudaeDataManager.get();
        PlayerData data = mgr.getPlayer(player.getUUID());

        if (!data.canClaim()) {
            long secs = data.getClaimCooldownRemaining() / 1000;
            player.sendSystemMessage(Component.literal(
                String.format("💔 Claim en cooldown. Disponible en %dh %dm.", secs / 3600, (secs % 3600) / 60))
                .withStyle(s -> s.withColor(0xFF5555)));
            return 0;
        }

        if (data.hasCharacter(charId)) {
            player.sendSystemMessage(Component.literal(
                "❌ Ya tenés a " + roll.name() + " en tu harem.")
                .withStyle(s -> s.withColor(0xFF5555)));
            return 0;
        }

        Character character = CharacterDatabase.getById(charId);
        if (character == null) {
            player.sendSystemMessage(Component.literal("Error: personaje no encontrado en la base de datos."));
            return 0;
        }

        data.claim(character);
        global.claimCharacter(charId);
        global.removePendingRoll(charId);
        mgr.savePlayer(player.getUUID());
        MudaeServerHandler.applyStats(player, data);

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
            false);

        return 1;
    }

    // ─── /harem ────────────────────────────────────────────────────────────
    private static int showHarem(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());

        if (data.getHarem().isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "💔 Tu harem está vacío. Usá el Altar de Mudae para conseguir personajes."), false);
            return 1;
        }

        MutableComponent msg = Component.literal(
            "═══ Tu Harem (" + data.getHarem().size() + " personajes) ═══\n")
            .withStyle(s -> s.withColor(0xFF69B4).withBold(true));

        for (int i = 0; i < data.getHarem().size(); i++) {
            Character c = data.getHarem().get(i);
            msg.append(Component.literal((i + 1) + ". ").withStyle(s -> s.withColor(0x808080)))
               .append(Component.literal(c.name()).withStyle(s -> s
                    .withColor(0xFFD700)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal("📺 " + c.animeName() + "\n💎 " + c.kakeraValue() + " kakera  |  Top " + c.id())))))
               .append(Component.literal(" — " + c.animeName() + "\n").withStyle(s -> s.withColor(0xADD8E6)));
        }

        msg.append(Component.literal("💎 Kakera total: " + data.getKakera())
            .withStyle(s -> s.withColor(0x9370DB)));

        ctx.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    // ─── /kakera ───────────────────────────────────────────────────────────
    private static int showKakera(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());
        ctx.getSource().sendSuccess(() -> Component.literal(
            "💎 Tenés " + data.getKakera() + " kakera.").withStyle(s -> s.withColor(0x9370DB)), false);
        return 1;
    }
}
