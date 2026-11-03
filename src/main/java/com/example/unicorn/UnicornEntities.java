package com.example.unicorn;

import com.example.magicwand.MagicWandMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public class UnicornEntities {

    public static final ResourceKey<EntityType<?>> UNICORN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            MagicWandMod.MOD_ID,
                            "unicorn"
                    )
            );

    public static final EntityType<UnicornEntity> UNICORN =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    UNICORN_KEY,
                    EntityType.Builder.of(
                                    UnicornEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(1.396f, 1.6f)
                            .build(UNICORN_KEY)
            );

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(
                UNICORN,
                AbstractHorse.createBaseHorseAttributes()
        );
    }
}