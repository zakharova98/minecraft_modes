package com.example.magicwand.command;

import com.example.magicwand.skin.SkinServer;
import com.example.magicwand.skin.SkinStore;
import com.example.magicwand.skin.SkinStore.ModelPref;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * /skin                  - справка
 * /skin model auto|slim|wide - тип рук: auto (по файлу), slim (Алекс), wide (Стив)
 * /skin info             - что сейчас сохранено на сервере
 * /skin reset            - убрать свой кастомный скин
 * (/setskin - старое название, работает как /skin)
 */
public class SkinCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            LiteralArgumentBuilder<CommandSourceStack> model = Commands.literal("model");
            for (ModelPref pref : ModelPref.values()) {
                model.then(Commands.literal(pref.name().toLowerCase(Locale.ROOT))
                        .executes(ctx -> setModel(ctx.getSource(), pref)));
            }

            LiteralCommandNode<CommandSourceStack> skin = dispatcher.register(
                    Commands.literal("skin")
                            .executes(ctx -> help(ctx.getSource()))
                            .then(model)
                            .then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))
                            .then(Commands.literal("reset").executes(ctx -> reset(ctx.getSource())))
            );
            dispatcher.register(Commands.literal("setskin").executes(ctx -> help(ctx.getSource())).redirect(skin));
        });
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal(
                "§e[Скин] §fПоложи PNG (64x64) в §bconfig/magicwand/player_skin.png§f - он загрузится сам, даже во время игры.\n"
                        + "§e/skin model slim§f - тонкие руки (Алекс), §e/skin model wide§f - обычные (Стив), §e/skin model auto§f - по файлу.\n"
                        + "§e/skin info§f, §e/skin reset§f"), false);
        return 1;
    }

    private static int setModel(CommandSourceStack src, ModelPref pref) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        SkinServer.setPref(src.getServer(), player.getUUID(), pref);
        String name = switch (pref) {
            case SLIM -> "тонкие (Алекс)";
            case WIDE -> "обычные (Стив)";
            case AUTO -> "автоматически по скину";
        };
        src.sendSuccess(() -> Component.literal("§a[Скин] Руки: " + name), false);
        return 1;
    }

    private static int info(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        SkinStore.Entry e = SkinStore.get(player.getUUID());
        String text;
        if (e == null) {
            text = "§e[Скин] На сервере для тебя ничего не сохранено.";
        } else {
            text = "§e[Скин] §fСкин: " + (e.hasSkin() ? "загружен (" + e.png().length + " байт)" : "нет")
                    + "§f, настройка рук: §b" + e.pref().name().toLowerCase(Locale.ROOT)
                    + "§f, сейчас: §b" + (e.slim() ? "slim (Алекс)" : "wide (Стив)");
        }
        src.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int reset(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        SkinServer.reset(src.getServer(), player.getUUID());
        src.sendSuccess(() -> Component.literal(
                "§a[Скин] Кастомный скин убран. Чтобы он не вернулся при следующем входе, удали config/magicwand/player_skin.png"), false);
        return 1;
    }
}
