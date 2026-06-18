package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.network.handler.CasinoServerHandler;
import com.mudaemod.mudaemod.network.handler.MudaeClientHandler;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MudaeNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("3");

        reg.playToServer(SellPayload.TYPE,       SellPayload.STREAM_CODEC,       MudaeServerHandler::handleSell);
        reg.playToServer(BuyStatPayload.TYPE,    BuyStatPayload.STREAM_CODEC,    MudaeServerHandler::handleBuyStat);
        reg.playToServer(CasinoBetPayload.TYPE,  CasinoBetPayload.STREAM_CODEC,  CasinoServerHandler::handleBet);
        reg.playToServer(CasinoActionPayload.TYPE, CasinoActionPayload.STREAM_CODEC, CasinoServerHandler::handleAction);

        reg.playToClient(HaremPayload.TYPE,        HaremPayload.STREAM_CODEC,        MudaeClientHandler::handleHarem);
        reg.playToClient(CasinoResultPayload.TYPE, CasinoResultPayload.STREAM_CODEC, MudaeClientHandler::handleCasinoResult);
    }
}
