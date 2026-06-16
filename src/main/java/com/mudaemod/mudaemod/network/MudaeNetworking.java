package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.network.handler.MudaeClientHandler;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MudaeNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("1");

        reg.playToServer(
            RollRequestPayload.TYPE,
            RollRequestPayload.STREAM_CODEC,
            MudaeServerHandler::handleRoll
        );

        reg.playToServer(
            ClaimRequestPayload.TYPE,
            ClaimRequestPayload.STREAM_CODEC,
            MudaeServerHandler::handleClaim
        );

        reg.playToClient(
            CharacterResultPayload.TYPE,
            CharacterResultPayload.STREAM_CODEC,
            MudaeClientHandler::handleCharacterResult
        );
    }
}
