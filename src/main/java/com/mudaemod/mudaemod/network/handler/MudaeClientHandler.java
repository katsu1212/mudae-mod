package com.mudaemod.mudaemod.network.handler;

import com.mudaemod.mudaemod.gui.MudaeScreen;
import com.mudaemod.mudaemod.network.HaremPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public class MudaeClientHandler {

    public static volatile HaremPayload lastHaremData = null;

    public static void handleHarem(HaremPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            lastHaremData = payload;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof MudaeScreen screen) {
                screen.onHaremReceived(payload);
            }
        });
    }
}
