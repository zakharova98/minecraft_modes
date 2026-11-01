package com.example.voidmod;

import com.example.voidmod.entity.BlackHoleEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final ResourceKey<EntityType<?>> BLACK_HOLE_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    VoidCraft.id("black_hole")
            );

    public static final EntityType<BlackHoleEntity> BLACK_HOLE =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    BLACK_HOLE_KEY,
                    EntityType.Builder
                            .<BlackHoleEntity>of(
                                    BlackHoleEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(BLACK_HOLE_KEY)
            );

    public static void initialize() {
        VoidCraft.LOGGER.info("VoidCraft: сущности зарегистрированы!");
    }
}