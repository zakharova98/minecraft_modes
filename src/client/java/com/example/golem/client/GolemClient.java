package com.example.golem.client;

import com.example.golem.GolemEntities;
import com.example.magicwand.client.ClientEvents;
import com.example.magicwand.client.SkinManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import com.example.golem.menu.GolemMenus;

public class GolemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SkinManager.init();
        ClientEvents.register();

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