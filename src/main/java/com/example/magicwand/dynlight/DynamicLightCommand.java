package com.example.magicwand.dynlight;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** /dynlight [on|off|reload] - управление динамическим светом. */
public class DynamicLightCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("dynlight")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§e[Свет] §fДинамический свет: " + (DynamicLights.isEnabled() ? "§aвключён" : "§cвыключен")
                                            + "§f, предметов в списке: " + DynamicLights.itemCount()
                                            + "\n§e/dynlight on§f, §e/dynlight off§f, §e/dynlight reload§f (перечитать config/magicwand/dynamic_light.txt)"), false);
                            return 1;
                        })
                        .then(Commands.literal("on").executes(ctx -> {
                            DynamicLights.setEnabled(true);
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Свет] Динамический свет включён"), true);
                            return 1;
                        }))
                        .then(Commands.literal("off").executes(ctx -> {
                            DynamicLights.setEnabled(false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§c[Свет] Динамический свет выключен"), true);
                            return 1;
                        }))
                        .then(Commands.literal("reload").executes(ctx -> {
                            DynamicLights.load();
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Свет] Настройки перечитаны, предметов: " + DynamicLights.itemCount()), false);
                            return 1;
                        }))
                ));
    }
}
