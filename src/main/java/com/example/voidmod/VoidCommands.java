package com.example.voidmod;

import com.example.voidmod.entity.BlackHoleEntity;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class VoidCommands {

    public static void register() {

        VoidCraft.LOGGER.info("Регистрация команд VoidCraft...");

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    VoidCraft.LOGGER.info(
                            "Команды VoidCraft регистрируются на сервере!"
                    );

                    dispatcher.register(
                            Commands.literal("voidcraft")
                                    .then(
                                            Commands.literal("create")
                                                    .executes(context -> {

                                                        ServerPlayer player =
                                                                context.getSource()
                                                                        .getPlayerOrException();

                                                        ServerLevel level =
                                                                (ServerLevel) player.level();

                                                        Vec3 look =
                                                                player.getLookAngle();

                                                        Vec3 position =
                                                                player.position()
                                                                        .add(look.scale(3.0));

                                                        BlackHoleEntity blackHole =
                                                                new BlackHoleEntity(
                                                                        ModEntities.BLACK_HOLE,
                                                                        level
                                                                );

                                                        blackHole.setPos(
                                                                position.x,
                                                                position.y,
                                                                position.z
                                                        );

                                                        level.addFreshEntity(
                                                                blackHole
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Чёрная дыра создана!"
                                                                ),
                                                                true
                                                        );

                                                        return 1;
                                                    })
                                    )
                    );
                }
        );
    }
}