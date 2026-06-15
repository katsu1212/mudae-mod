package com.mudaemod.mudaemod;

import com.mudaemod.mudaemod.command.MudaeCommands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MudaeMod.MODID)
public class MudaeMod {
    public static final String MODID = "mudaemod";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public MudaeMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MudaeCommands.register(event.getDispatcher());
    }
}
