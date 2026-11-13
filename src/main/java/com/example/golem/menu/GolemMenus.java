package com.example.golem.menu;

import com.example.magicwand.MagicWandMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlagSet;

public class GolemMenus {

    public static final ResourceKey<MenuType<?>> STORAGE_GOLEM_KEY =
            ResourceKey.create(
                    Registries.MENU,
                    Identifier.fromNamespaceAndPath(
                            MagicWandMod.MOD_ID,
                            "storage_golem"
                    )
            );

    public static final MenuType<GolemMenu> STORAGE_GOLEM =
            Registry.register(
                    BuiltInRegistries.MENU,
                    STORAGE_GOLEM_KEY,
                    new MenuType<>(GolemMenu::new, FeatureFlagSet.of())
            );

    public static void initialize() {
        // Регистрация выполняется при загрузке класса.
    }
}