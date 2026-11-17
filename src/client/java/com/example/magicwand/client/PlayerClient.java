package com.example.magicwand.client;

import net.fabricmc.api.ClientModInitializer;

public class PlayerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkinManager.init();
        ClientEvents.register();
    }
}
