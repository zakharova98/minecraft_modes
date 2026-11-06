package com.example.unicorn.network;

import com.example.unicorn.UnicornEntity;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class UnicornSprintNetworking {

    public static void initialize() {

        PayloadTypeRegistry.serverboundPlay().register(
                UnicornSprintPayload.TYPE,
                UnicornSprintPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UnicornSprintPayload.TYPE,
                (payload, context) -> {

                    var player = context.player();

                    if (player.getVehicle() instanceof UnicornEntity unicorn
                            && unicorn.getControllingPassenger() == player) {

                        unicorn.setMagicalSprint(payload.active());
                    }
                }
        );
    }
}