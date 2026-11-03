package com.example.unicorn.client;

import com.example.unicorn.UnicornEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class UnicornClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(
                UnicornEntities.UNICORN,
                UnicornRenderer::new
        );
    }
}
