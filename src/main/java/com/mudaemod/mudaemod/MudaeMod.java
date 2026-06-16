package com.mudaemod.mudaemod;

import com.mudaemod.mudaemod.block.MudaeBlock;
import com.mudaemod.mudaemod.data.MudaeDataManager;
import com.mudaemod.mudaemod.data.PlayerData;
import com.mudaemod.mudaemod.event.ChatCommandHandler;
import com.mudaemod.mudaemod.gui.MudaeMenu;
import com.mudaemod.mudaemod.network.MudaeNetworking;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MudaeMod.MODID)
public class MudaeMod {
    public static final String MODID = "mudaemod";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<Block, MudaeBlock> MUDAE_ALTAR =
        BLOCKS.register("mudae_altar", MudaeBlock::new);

    public static final DeferredHolder<Item, BlockItem> MUDAE_ALTAR_ITEM =
        ITEMS.register("mudae_altar", () -> new BlockItem(MUDAE_ALTAR.get(), new Item.Properties()));

    public static final DeferredHolder<MenuType<?>, MenuType<MudaeMenu>> MUDAE_MENU =
        MENUS.register("mudae_menu", () -> IMenuTypeExtension.create((id, inv, buf) -> new MudaeMenu(id, inv)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MUDAE_TAB =
        TABS.register("mudae_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Mudae"))
            .icon(() -> new ItemStack(MUDAE_ALTAR_ITEM.get()))
            .displayItems((p, o) -> o.accept(MUDAE_ALTAR_ITEM.get()))
            .build());

    public MudaeMod(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        TABS.register(modEventBus);

        modEventBus.addListener(MudaeNetworking::register);

        NeoForge.EVENT_BUS.addListener((ServerStartingEvent e) ->
            MudaeDataManager.get().init(e.getServer()));

        NeoForge.EVENT_BUS.addListener(ChatCommandHandler::onChat);

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer sp) {
                PlayerData data = MudaeDataManager.get().getPlayer(sp.getUUID());
                MudaeServerHandler.applyStats(sp, data);
            }
        });
    }
}
