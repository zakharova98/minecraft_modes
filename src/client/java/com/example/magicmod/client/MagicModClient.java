package com.example.magicmod.client;

import net.fabricmc.api.ClientModInitializer;

/** Client entrypoint retained for compatibility; grab interactions are handled server-side. */
public class MagicModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // No client-side GrabHandler is required.
    }
}
