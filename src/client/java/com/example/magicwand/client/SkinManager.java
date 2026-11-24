package com.example.magicwand.client;

import com.example.magicwand.network.SkinPayload;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinManager {
    private static final Map<UUID, Identifier> CUSTOM_SKINS = new HashMap<>();
    private static File pendingSkinFile = null;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SkinPayload.TYPE, (payload, context) -> {
            UUID uuid = payload.playerUuid();
            byte[] bytes = payload.skinBytes();

            try {
                // Читаем байты как NativeImage
                NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(bytes));
                if (nativeImage != null) {
                    Identifier textureId = Identifier.fromNamespaceAndPath("magicwand", "skins/" + uuid.toString());

                    context.client().execute(() -> {
                        // В версии 26.3 конструктор требует String (метку) и NativeImage
                        DynamicTexture dynamicTexture = new DynamicTexture(() -> "skin_" + uuid, nativeImage);
                        context.client().getTextureManager().register(textureId, dynamicTexture);
                        CUSTOM_SKINS.put(uuid, textureId);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingSkinFile != null) {
                uploadSkin(pendingSkinFile);
                pendingSkinFile = null;
            }
        });
    }

    public static Identifier getSkin(UUID uuid) {
        return CUSTOM_SKINS.get(uuid);
    }

    public static void requestUpload(File file) {
        pendingSkinFile = file;
    }

    public static void uploadSkin(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();

            if (bytes.length > 1024 * 1024) {
                // Используем sendSystemMessage вместо удаленного displayClientMessage
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.literal("§cФайл слишком большой (макс 1 МБ)!")
                );
                return;
            }

            UUID myUuid = Minecraft.getInstance().player.getUUID();
            ClientPlayNetworking.send(new SkinPayload(myUuid, bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}