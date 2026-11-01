package com.example.voidmod.client;

import com.example.voidmod.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class VoidCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                ModEntities.BLACK_HOLE,
                BlackHoleRenderer::new
        );
    }
}