package com.example.magicwand;

import com.example.golem.GolemEntities;
import com.example.golem.GolemItems;
import com.example.golem.menu.GolemMenus;
import com.example.unicorn.UnicornEntities;
import com.example.unicorn.network.UnicornSprintNetworking;

import com.example.magicwand.network.SkinPayload;
import com.example.magicwand.network.SkinSyncPayload;
import com.example.magicwand.network.SkinUploadPayload;
import com.example.magicwand.skin.SkinServer;
import com.example.magicwand.dynlight.DynamicLights;
import com.example.magicwand.dynlight.DynamicLightCommand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MagicWandMod implements ModInitializer {
    public static final String MOD_ID = "magicwand";

    // --- КОМПОНЕНТЫ ДАННЫХ ---
    public static final DataComponentType<TeleportData> TELEPORT_DATA =
            Registry.register(
                    BuiltInRegistries.DATA_COMPONENT_TYPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, "teleport_data"),
                    DataComponentType.<TeleportData>builder()
                            .persistent(TeleportData.CODEC)
                            .networkSynchronized(TeleportData.STREAM_CODEC)
                            .build()
            );

    // --- МАГИЧЕСКАЯ ПАЛОЧКА ---
    public static final ResourceKey<Item> MAGIC_WAND_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "magic_wand")
            );

    public static final Item MAGIC_WAND = Registry.register(
            BuiltInRegistries.ITEM,
            MAGIC_WAND_KEY,
            new MagicWandItem(new Item.Properties().setId(MAGIC_WAND_KEY))
    );

    // --- ТЕЛЕПОРТ ---
    public static final ResourceKey<Item> TELEPORT_ITEM_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "teleport_item")
            );

    public static final Item TELEPORT_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            TELEPORT_ITEM_KEY,
            new TeleportItem(
                    new Item.Properties()
                            .setId(TELEPORT_ITEM_KEY)
                            .stacksTo(1)
            )
    );

    // --- ФОНАРИК ---
    public static final ResourceKey<Item> FLASHLIGHT_OFF_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, "flashlight_off")
            );

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

    // --- СНАЙПЕРСКАЯ ВИНТОВКА ---
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

    // --- ПАТРОНЫ ---
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

    // --- ЯДРО ГОЛЕМА ---
    public static final ResourceKey<Item> GOLEM_CORE =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("golem", "golem_core")
            );

    @Override
    public void onInitialize() {

        ModSounds.initialize();
        ServerTickEvents.END_SERVER_TICK.register(SniperBolt::tick);

        // Вкладка "Инструменты"
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(tab -> {
                    tab.accept(new ItemStack(MAGIC_WAND));
                    tab.accept(new ItemStack(FLASHLIGHT_OFF));
                    tab.accept(new ItemStack(TELEPORT_ITEM));
                });

        UnicornEntities.initialize();
        UnicornSprintNetworking.initialize();

        // Вкладка "Бой"
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(tab -> {
                    tab.accept(new ItemStack(SNIPER_RIFLE));
                    tab.accept(new ItemStack(SNIPER_AMMO));
                });

        GolemItems.initialize();
        GolemEntities.initialize();
        GolemMenus.initialize();

        // --- ИНИЦИАЛИЗАЦИЯ СЕТИ И КОМАНД ---
        SkinSyncPayload.register();
        SkinUploadPayload.register();
        SkinServer.init();
        SkinCommand.register();

        // --- ДИНАМИЧЕСКИЙ СВЕТ (встроенный, без сторонних модов) ---
        DynamicLights.init();
        DynamicLightCommand.register();
    }
}