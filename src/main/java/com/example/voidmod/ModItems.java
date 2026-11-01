
package com.example.voidmod;

import com.example.voidmod.item.BlackHoleItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final ResourceKey<Item> VOID_CORE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("voidcraft", "void_core")
            );

    public static final Item VOID_CORE = Registry.register(
            BuiltInRegistries.ITEM,
            VOID_CORE_KEY,
            new BlackHoleItem(
                    new Item.Properties()
                            .setId(VOID_CORE_KEY)
                            .stacksTo(16)
            )
    );

    public static void initialize() {
        VoidCraft.LOGGER.info("VoidCraft: предметы зарегистрированы!");
    }

    public static void register() {
        // Вызывает регистрацию предмета при загрузке класса
    }
}
