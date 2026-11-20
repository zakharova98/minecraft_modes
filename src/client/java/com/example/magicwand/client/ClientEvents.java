package com.example.magicwand.client;

import com.example.magicwand.network.SkinUploadPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Следит за файлом config/magicwand/player_skin.png:
 * - загружает скин на сервер при входе в мир;
 * - загружает заново, если файл изменился прямо во время игры (проверка раз в секунду).
 */
public class ClientEvents {

    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("magicwand");
    private static final Path SKIN_FILE = DIR.resolve("player_skin.png");

    private static boolean sentThisSession = false;
    private static long lastStamp = -1;
    private static int tickCounter = 0;

    public static void register() {
        try {
            Files.createDirectories(DIR);
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось создать " + DIR + ": " + e);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                sentThisSession = false;
                lastStamp = -1;
                SkinManager.clear();
                return;
            }

            boolean first = !sentThisSession;
            if (!first && (++tickCounter % 20 != 0)) return;

            long stamp = currentStamp();
            if (!first && stamp == lastStamp) return;

            sentThisSession = true;
            lastStamp = stamp;
            if (stamp != -1) {
                upload(client, !first);
            }
        });
    }

    /** "Отпечаток" файла: время изменения + размер. -1, если файла нет. */
    private static long currentStamp() {
        try {
            if (!Files.isRegularFile(SKIN_FILE)) return -1;
            return Files.getLastModifiedTime(SKIN_FILE).toMillis() * 31 + Files.size(SKIN_FILE);
        } catch (IOException e) {
            return -1;
        }
    }

    private static void upload(Minecraft client, boolean announce) {
        try {
            SkinImageProcessor.Result result = SkinImageProcessor.process(SKIN_FILE);
            ClientPlayNetworking.send(new SkinUploadPayload(result.detectedSlim(), result.png()));
            if (announce) {
                client.player.sendSystemMessage(Component.literal("§a[Скин] Скин обновлён и отправлен на сервер"));
            }
        } catch (IOException e) {
            client.player.sendSystemMessage(Component.literal("§c[Скин] Не удалось загрузить player_skin.png: " + e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("[magicwand] Ошибка отправки скина: " + e);
        }
    }
}