package com.mudaemod.mudaemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
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
    }

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
                        Component.literal("📺 " + c.animeName() + "\n💎 " + c.kakeraValue() + " kakera")))))
               .append(Component.literal(" — " + c.animeName() + "\n").withStyle(s -> s.withColor(0xADD8E6)));
        }

        msg.append(Component.literal("💎 Kakera total: " + data.getKakera())
            .withStyle(s -> s.withColor(0x9370DB)));

        ctx.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int showKakera(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());
        ctx.getSource().sendSuccess(() -> Component.literal(
            "💎 Tenés " + data.getKakera() + " kakera.").withStyle(s -> s.withColor(0x9370DB)), false);
        return 1;
    }
}
