package com.example.magicwand;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class MagicWandMod implements ModInitializer {
    public static final String MOD_ID = "magicwand";

    public static final ResourceKey<Item> MAGIC_WAND_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(
                            MOD_ID, "magic_wand"
                    )
            );

    public static final Item MAGIC_WAND = Registry.register(
            BuiltInRegistries.ITEM,
            MAGIC_WAND_KEY,
            new MagicWandItem(new Item.Properties().setId(MAGIC_WAND_KEY))
    );

    public static final ResourceKey<Item> FLASHLIGHT_OFF_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "flashlight_off"));

    public static final ResourceKey<Item> FLASHLIGHT_ON_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "flashlight_on")
            );

    public static final Item FLASHLIGHT_OFF = Registry.register(
            BuiltInRegistries.ITEM,
            FLASHLIGHT_OFF_KEY,
            new FlashlightItem(
                    new Item.Properties()
                            .setId(FLASHLIGHT_OFF_KEY)
                            .stacksTo(1),
                    () -> MagicWandMod.FLASHLIGHT_ON
            )
    );

    public static final Item FLASHLIGHT_ON = Registry.register(
            BuiltInRegistries.ITEM,
            FLASHLIGHT_ON_KEY,
            new FlashlightItem(
                    new Item.Properties()
                            .setId(FLASHLIGHT_ON_KEY)
                            .stacksTo(1),
                    () -> MagicWandMod.FLASHLIGHT_OFF
            )
    );

    @Override
    public void onInitialize() {
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(tab -> {
                    tab.accept(MAGIC_WAND);
                    tab.accept(FLASHLIGHT_OFF);
                });
    }
}
