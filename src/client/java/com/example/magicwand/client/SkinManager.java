package com.example.magicwand.client;

import com.example.magicwand.network.SkinSyncPayload;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Хранит кастомные скины всех игроков, которые прислал сервер. Миксин берёт скины отсюда. */
public class SkinManager {

    private static final Map<UUID, PlayerSkin> SKINS = new ConcurrentHashMap<>();

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SkinSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> apply(context.client(), payload)));
    }

    /** Вызывается миксином при каждой отрисовке игрока, поэтому только быстрый lookup. */
    public static PlayerSkin getPlayerSkin(UUID uuid) {
        return SKINS.get(uuid);
    }

    /** Сбрасываем всё при выходе из мира, чтобы скины одного сервера не "протекли" на другой. */
    public static void clear() {
        if (!SKINS.isEmpty()) {
            SKINS.clear();
        }
    }

    private static void apply(Minecraft client, SkinSyncPayload payload) {
        UUID uuid = payload.playerUuid();

        if (payload.png().length == 0) {
            SKINS.remove(uuid); // у игрока больше нет кастомного скина
            return;
        }

        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(payload.png()));
            Identifier textureId = Identifier.fromNamespaceAndPath("magicwand", "skins/" + uuid);

            // Повторная регистрация под тем же id закрывает и заменяет старую текстуру
            DynamicTexture texture = new DynamicTexture(() -> "skin_" + uuid, image);
            client.getTextureManager().register(textureId, texture);

            SKINS.put(uuid, buildPlayerSkin(textureId, payload.slim()));
        } catch (IOException | RuntimeException e) {
            System.err.println("[magicwand] Не удалось применить скин " + uuid + ": " + e);
        }
    }

    private static PlayerSkin buildPlayerSkin(Identifier textureId, boolean slim) {
        ClientAsset.Texture bodyTexture = new ClientAsset.Texture() {
            @Override public Identifier id() { return textureId; }
            @Override public Identifier texturePath() { return textureId; }
        };
        // WIDE - руки как у Стива (4 пикселя), SLIM - как у Алекс (3 пикселя)
        PlayerModelType model = slim ? PlayerModelType.SLIM : PlayerModelType.WIDE;
        return new PlayerSkin(bodyTexture, null, null, model, false);
    }
}
