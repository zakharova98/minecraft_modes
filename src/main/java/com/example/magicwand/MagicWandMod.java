package com.example.magicwand;

import com.example.unicorn.UnicornEntities;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import com.example.unicorn.network.UnicornSprintNetworking;

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

        UnicornEntities.initialize();
        UnicornSprintNetworking.initialize();

        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(tab -> {
                    tab.accept(SNIPER_RIFLE);
                    tab.accept(SNIPER_AMMO);
                });
    }

    // Снайперская винтовка
    public static final ResourceKey<Item> SNIPER_RIFLE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "sniper_rifle")
            );

    public static final Item SNIPER_RIFLE = Registry.register(
            BuiltInRegistries.ITEM,
            SNIPER_RIFLE_KEY,
            new SniperRifleItem(
                    new Item.Properties()
                            .setId(SNIPER_RIFLE_KEY)
                            .stacksTo(1)
            )
    );

    // Патроны для снайперки
    public static final ResourceKey<Item> SNIPER_AMMO_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "sniper_ammo")
            );

    public static final Item SNIPER_AMMO = Registry.register(
            BuiltInRegistries.ITEM,
            SNIPER_AMMO_KEY,
            new Item(
                    new Item.Properties()
                            .setId(SNIPER_AMMO_KEY)
                            .stacksTo(64)
            )
    );
}
