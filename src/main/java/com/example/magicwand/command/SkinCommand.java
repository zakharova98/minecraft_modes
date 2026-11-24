package com.example.magicwand.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SkinCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("setskin")
                    .executes(context -> {
                        context.getSource().sendSuccess(() -> Component.literal("§e[Скин] Положите player_skin.png в config/magicwand/ и зайдите в мир."), false);
                        return 1;
                    })
            );
        });
    }
}