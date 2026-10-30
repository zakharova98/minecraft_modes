
package com.example.voidmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {

    private static final String MOD_ID = "voidcraft";

    private static ResourceKey<Item> createKey(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, name)
        );
    }

    private static Item register(
            ResourceKey<Item> key,
            Function<Item.Properties, Item> factory
    ) {
        Item.Properties properties = new Item.Properties().setId(key);
        Item item = factory.apply(properties);

        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                item
        );
    }

    public static final ResourceKey<Item> VOID_SHARD_KEY =
            createKey("void_shard");

    public static final Item VOID_SHARD =
            register(VOID_SHARD_KEY, VoidShardItem::new);

    public static final ResourceKey<Item> VOID_SPHERE_KEY =
            createKey("void_sphere");

    public static final Item VOID_SPHERE =
            register(VOID_SPHERE_KEY, VoidSphereItem::new);

    private ModItems() {
    }

    public static void initialize() {
        ServerTickEvents.END_LEVEL_TICK.register(
                VoidRiftManager::tick
        );

        ServerTickEvents.END_LEVEL_TICK.register(
                VoidBoltManager::tick
        );

        ServerTickEvents.END_LEVEL_TICK.register(
                VoidBlackHoleManager::tick
        );
    }
}
