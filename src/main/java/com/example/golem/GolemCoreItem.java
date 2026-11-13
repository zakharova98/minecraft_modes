package com.example.golem;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GolemCoreItem extends Item {

    public GolemCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        if (!level.isClientSide()) {

            GolemEntity golem =
                    GolemEntities.STORAGE_GOLEM.create(
                            level,
                            EntitySpawnReason.SPAWN_ITEM_USE
                    );

            if (golem != null) {

                golem.setPos(
                        player.getX(),
                        player.getY(),
                        player.getZ()
                );
                golem.setYRot(player.getYRot());
                golem.setXRot(0);

                level.addFreshEntity(golem);

                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}