
package com.example.golem;

import com.example.magicwand.MagicWandMod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.golem.IronGolem;

public class GolemEntities {

    public static final ResourceKey<EntityType<?>> GOLEM_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            MagicWandMod.MOD_ID,
                            "storage_golem"
                    )
            );

    public static final EntityType<GolemEntity> STORAGE_GOLEM =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    GOLEM_KEY,
                    EntityType.Builder
                            .<GolemEntity>of(
                                    GolemEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(1.4F, 2.7F)
                            .build(GOLEM_KEY)
            );

    public static void initialize() {

        FabricDefaultAttributeRegistry.register(
                STORAGE_GOLEM,
                IronGolem.createAttributes()
        );
    }
}
