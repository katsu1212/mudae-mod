package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.network.handler.MudaeClientHandler;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MudaeNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("2");

        reg.playToServer(
            SellPayload.TYPE,
            SellPayload.STREAM_CODEC,
            MudaeServerHandler::handleSell
        );

        reg.playToServer(
            BuyStatPayload.TYPE,
            BuyStatPayload.STREAM_CODEC,
            MudaeServerHandler::handleBuyStat
        );

        reg.playToClient(
            HaremPayload.TYPE,
            HaremPayload.STREAM_CODEC,
            MudaeClientHandler::handleHarem
        );
    }
}
