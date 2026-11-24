package com.example.golem.client;

import com.example.golem.GolemEntities;
import com.example.golem.menu.GolemMenus;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.gui.screens.MenuScreens;

public class GolemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRenderers.register(
                GolemEntities.STORAGE_GOLEM,
                IronGolemRenderer::new
        );

        MenuScreens.register(
                GolemMenus.STORAGE_GOLEM,
                GolemScreen::new
        );
    }
}