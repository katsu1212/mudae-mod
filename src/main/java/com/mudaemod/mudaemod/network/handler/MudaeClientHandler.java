package com.mudaemod.mudaemod.network.handler;

import com.mudaemod.mudaemod.gui.MudaeScreen;
import com.mudaemod.mudaemod.network.CharacterResultPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public class MudaeClientHandler {

    public static void handleCharacterResult(CharacterResultPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof MudaeScreen screen) {
                screen.onCharacterReceived(payload);
            }
        });
    }
}
