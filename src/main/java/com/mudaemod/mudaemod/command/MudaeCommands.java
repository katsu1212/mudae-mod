package com.mudaemod.mudaemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mudaemod.mudaemod.data.ActiveRoll;
import com.mudaemod.mudaemod.data.Character;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.network.AniListClient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MudaeCommands {

    // Rolls activos en el servidor: channelId (usamos server global) -> ActiveRoll
    private static final Map<UUID, ActiveRoll> activeRolls = new ConcurrentHashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /w — roll waifu (personaje femenino preferido)
        dispatcher.register(Commands.literal("w")
                .executes(ctx -> roll(ctx, true)));

        // /h — roll husbando (personaje masculino preferido)
        dispatcher.register(Commands.literal("h")
                .executes(ctx -> roll(ctx, false)));

        // /harem — ver tu colección
        dispatcher.register(Commands.literal("harem")
                .executes(MudaeCommands::showHarem));

        // /marry — claimear el personaje activo
        dispatcher.register(Commands.literal("marry")
                .executes(MudaeCommands::marry));

        // /kakera — ver tus kakera
        dispatcher.register(Commands.literal("kakera")
                .executes(MudaeCommands::showKakera));
    }

    private static int roll(CommandContext<CommandSourceStack> ctx, boolean waifu) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());

        if (!data.canRoll()) {
            long remaining = data.getRollCooldownRemaining() / 1000;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            source.sendFailure(Component.literal(
                String.format("⏳ No te quedan rolls. Recarga en %dm %ds.", minutes, seconds)
            ));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("🎲 Invocando personaje..."), false);
        data.useRoll();

        AniListClient.rollRandom(waifu).thenAccept(optChar -> {
            if (optChar.isEmpty()) {
                player.sendSystemMessage(Component.literal("❌ No se pudo obtener un personaje. Intenta de nuevo."));
                return;
            }
            Character character = optChar.get();

            // Guardamos el roll activo usando el id del personaje como clave
            UUID rollKey = UUID.nameUUIDFromBytes(("roll_" + character.id()).getBytes());
            activeRolls.put(rollKey, new ActiveRoll(character, player.getUUID()));

            // Construimos el mensaje con el personaje
            MutableComponent msg = Component.literal("").append(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━\n")
                    .withStyle(s -> s.withColor(0xFF69B4))
            ).append(
                Component.literal("✨ " + character.name() + "\n")
                    .withStyle(s -> s.withColor(0xFFD700).withBold(true))
            ).append(
                Component.literal("📺 " + character.animeName() + "\n")
                    .withStyle(s -> s.withColor(0xADD8E6))
            ).append(
                Component.literal("[🔗 Ver imagen]\n")
                    .withStyle(s -> s
                        .withColor(0x00BFFF)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, character.imageUrl()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.literal("Click para ver la imagen de " + character.name()))))
            ).append(
                Component.literal("[💍 /marry] ")
                    .withStyle(s -> s
                        .withColor(0xFF69B4)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marry"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.literal("¡Claimear a " + character.name() + "!"))))
            ).append(
                Component.literal("(3 min)\n").withStyle(s -> s.withColor(0x808080))
            ).append(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(s -> s.withColor(0xFF69B4))
            );

            // Enviamos a todos en el servidor para que puedan competir por el claim
            player.getServer().getPlayerList().broadcastSystemMessage(msg, false);
        });

        MudaeDataManager.get().savePlayer(player.getUUID());
        return 1;
    }

    private static int marry(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());

        if (!data.canClaim()) {
            source.sendFailure(Component.literal("💔 Ya tienes un personaje. Espera para claimear otro."));
            return 0;
        }

        // Buscamos el roll activo más reciente no expirado
        Optional<Map.Entry<UUID, ActiveRoll>> rollEntry = activeRolls.entrySet().stream()
                .filter(e -> !e.getValue().isExpired())
                .findFirst();

        if (rollEntry.isEmpty()) {
            source.sendFailure(Component.literal("❌ No hay ningún personaje disponible para claimear ahora."));
            return 0;
        }

        Map.Entry<UUID, ActiveRoll> entry = rollEntry.get();
        ActiveRoll roll = entry.getValue();
        Character character = roll.character;

        if (data.hasCharacter(character.id())) {
            source.sendFailure(Component.literal("❌ Ya tienes a " + character.name() + " en tu harem."));
            return 0;
        }

        data.claim(character);
        activeRolls.remove(entry.getKey());
        MudaeDataManager.get().savePlayer(player.getUUID());

        MutableComponent msg = Component.literal("").append(
            Component.literal("💍 ").withStyle(s -> s.withColor(0xFFD700))
        ).append(
            Component.literal(player.getName().getString()).withStyle(s -> s.withColor(0x00FF7F).withBold(true))
        ).append(
            Component.literal(" ¡se casó con ").withStyle(s -> s.withColor(0xFFFFFF))
        ).append(
            Component.literal(character.name()).withStyle(s -> s.withColor(0xFF69B4).withBold(true))
        ).append(
            Component.literal(" de " + character.animeName() + "!").withStyle(s -> s.withColor(0xADD8E6))
        );

        player.getServer().getPlayerList().broadcastSystemMessage(msg, false);
        return 1;
    }

    private static int showHarem(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());

        if (data.getHarem().isEmpty()) {
            source.sendSuccess(() -> Component.literal("💔 Tu harem está vacío. Usa /w o /h para conseguir personajes."), false);
            return 1;
        }

        MutableComponent msg = Component.literal("").append(
            Component.literal("═══ Tu Harem (" + data.getHarem().size() + " personajes) ═══\n")
                .withStyle(s -> s.withColor(0xFF69B4).withBold(true))
        );

        for (int i = 0; i < data.getHarem().size(); i++) {
            Character c = data.getHarem().get(i);
            int index = i + 1;
            msg.append(
                Component.literal(index + ". ")
                    .withStyle(s -> s.withColor(0x808080))
            ).append(
                Component.literal(c.name())
                    .withStyle(s -> s
                        .withColor(0xFFD700)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, c.imageUrl()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.literal("Ver imagen de " + c.name() + "\n📺 " + c.animeName()))))
            ).append(
                Component.literal(" — " + c.animeName() + "\n")
                    .withStyle(s -> s.withColor(0xADD8E6))
            );
        }

        msg.append(Component.literal("💎 Kakera: " + data.getKakera())
            .withStyle(s -> s.withColor(0x9370DB)));

        source.sendSuccess(() -> msg, false);
        return 1;
    }

    private static int showKakera(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerData data = MudaeDataManager.get().getPlayer(player.getUUID());
        source.sendSuccess(() -> Component.literal(
            "💎 Tienes " + data.getKakera() + " kakera."
        ).withStyle(s -> s.withColor(0x9370DB)), false);
        return 1;
    }
}
