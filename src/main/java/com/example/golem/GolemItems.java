package com.example.golem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class GolemItems {

    private static final String MOD_ID = "golem";

    public static final ResourceKey<Item> GOLEM_CORE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(
                            MOD_ID,
                            "golem_core"
                    )
            );

    public static final Item GOLEM_CORE =
            Registry.register(
                    BuiltInRegistries.ITEM,
                    GOLEM_CORE_KEY,
                    new GolemCoreItem(
                            new Item.Properties().setId(GOLEM_CORE_KEY)
                    )
            );


    public static final ResourceKey<Item> GOLEM_CONFIGURATOR_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(
                            MOD_ID,
                            "golem_configurator"
                    )
            );

    public static final Item GOLEM_CONFIGURATOR =
            Registry.register(
                    BuiltInRegistries.ITEM,
                    GOLEM_CONFIGURATOR_KEY,
                    new GolemConfiguratorItem(
                            new Item.Properties()
                                    .setId(GOLEM_CONFIGURATOR_KEY)
                    )
            );


    public static void initialize() {

        CreativeModeTabEvents.modifyOutputEvent(
                CreativeModeTabs.TOOLS_AND_UTILITIES
        ).register(tab -> {
            tab.accept(GOLEM_CORE);
            tab.accept(GOLEM_CONFIGURATOR);
        });
    }
}