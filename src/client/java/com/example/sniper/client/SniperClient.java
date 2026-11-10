package com.example.sniper.client;

import net.fabricmc.api.ClientModInitializer;

public class SniperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SniperScopeClient.init();
    }
}
