package com.mudaemod.mudaemod;

import com.mudaemod.mudaemod.gui.CasinoMenu;
import com.mudaemod.mudaemod.gui.CasinoScreen;
import com.mudaemod.mudaemod.gui.MudaeMenu;
import com.mudaemod.mudaemod.gui.MudaeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MudaeMod.MODID, value = Dist.CLIENT)
public class MudaeClientEvents {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(MudaeMod.MUDAE_MENU.get(),   MudaeScreen::new);
        event.register(MudaeMod.CASINO_MENU.get(), CasinoScreen::new);
    }
}
