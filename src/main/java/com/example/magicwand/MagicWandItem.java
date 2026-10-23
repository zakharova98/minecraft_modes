package com.example.magicwand;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MagicWandItem extends Item {

    public MagicWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (level instanceof ServerLevel serverLevel) {

            // Shift + ПКМ: призвать черного котика
            if (player.isShiftKeyDown()) {

                EntityType<?> catType =
                        BuiltInRegistries.ENTITY_TYPE.getValue(
                                Identifier.withDefaultNamespace("cat")
                        );

                Cat cat = (Cat) catType.create(
                        serverLevel,
                        EntitySpawnReason.MOB_SUMMONED
                );

                if (cat != null) {
                    var blackVariant = serverLevel.registryAccess()
                            .lookupOrThrow(Registries.CAT_VARIANT)
                            .getOrThrow(CatVariants.ALL_BLACK);

                    cat.setComponent(
                            DataComponents.CAT_VARIANT,
                            blackVariant
                    );

                    Vec3 look = player.getLookAngle();

                    cat.snapTo(
                            player.getX() + look.x * 1.5,
                            player.getY(),
                            player.getZ() + look.z * 1.5,
                            player.getYRot(),
                            0
                    );

                    cat.setCustomName(
                            Component.literal("Черный котик")
                    );
                    cat.setCustomNameVisible(true);
                    cat.tame(player);

                    serverLevel.addFreshEntity(cat);
                }

            } else {
                // Обычный ПКМ: выстрелить огненным шаром
                Vec3 direction = player.getLookAngle().scale(1.5);

                SmallFireball fireball =
                        new SmallFireball(level, player, direction);

                serverLevel.addFreshEntity(fireball);
            }
        }

        return InteractionResult.SUCCESS;
    }
}

