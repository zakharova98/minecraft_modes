package com.example.magicwand.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import java.io.File;

public class ClientEvents {
    private static boolean sentThisSession = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !sentThisSession) {
                // Ищем файл скина при заходе в мир
                File skinFile = new File("config/magicwand/robot_skin.png");
                if (skinFile.exists()) {
                    SkinManager.requestUpload(skinFile);
                    sentThisSession = true;
                }
            } else if (client.player == null) {
                sentThisSession = false; // Сбрасываем при выходе в меню
            }
        });
    }
}