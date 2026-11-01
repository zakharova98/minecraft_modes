package com.example.voidmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.world.item.CreativeModeTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoidCraft implements ModInitializer {

    public static final String MOD_ID = "voidcraft";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.initialize();

        ModEntities.initialize();
        VoidCommands.register();

        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(creativeTab ->
                        creativeTab.accept(ModItems.VOID_CORE)
                );

        LOGGER.info("VoidCraft: Гравитация Пустоты бесконечна!");
        LOGGER.info("VOID_CORE: {}", ModItems.VOID_CORE);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}