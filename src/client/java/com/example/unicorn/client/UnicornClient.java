package com.example.unicorn.client;

import com.example.unicorn.UnicornEntity;
import com.example.unicorn.network.UnicornSprintPayload;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.example.unicorn.UnicornEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.resources.Identifier;

public class UnicornClient implements ClientModInitializer {

    private static boolean lastSprintState = false;

    @Override
    public void onInitializeClient() {

        // Регистрируем модели единорога
        ModelLayerRegistry.registerModelLayer(
                UnicornModel.LAYER,
                UnicornModel::createLayer
        );

        ModelLayerRegistry.registerModelLayer(
                UnicornBabyModel.LAYER,
                UnicornBabyModel::createLayer
        );

        // Регистрация отображения единорога
        EntityRenderers.register(
                UnicornEntities.UNICORN,
                UnicornRenderer::new
        );

        KeyMapping.Category category =
                KeyMapping.Category.register(
                        Identifier.fromNamespaceAndPath(
                                "magicwand",
                                "unicorn"
                        )
                );

        // Клавиша G
        KeyMapping magicalSprintKey =
                KeyMappingHelper.registerKeyMapping(
                        new KeyMapping(
                                "key.magicwand.unicorn_sprint",
                                InputConstants.Type.KEYBOARD,
                                InputConstants.KEY_G,
                                category
                        )
                );

        // Проверяем состояние клавиши каждый игровой тик
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) {
                lastSprintState = false;
                return;
            }

            boolean shouldSprint =
                    client.player.getVehicle() instanceof UnicornEntity
                            && magicalSprintKey.isDown();

            // Отправляем пакет только при изменении состояния
            if (shouldSprint != lastSprintState) {

                ClientPlayNetworking.send(
                        new UnicornSprintPayload(shouldSprint)
                );

                lastSprintState = shouldSprint;
            }
        });
    }
}