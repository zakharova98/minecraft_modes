
package com.example.voidmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VoidShardItem extends Item {

    public VoidShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player user,
            InteractionHand hand
    ) {
        if (!level.isClientSide()
                && level instanceof ServerLevel serverLevel) {

            if (user.isShiftKeyDown()) {
                // Shift + ПКМ: запускаем снаряд.
                VoidBoltManager.create(serverLevel, user);
            } else {
                // Обычный ПКМ: открываем разлом.
                Vec3 center = user.getEyePosition()
                        .add(user.getLookAngle().scale(3.0));

                VoidRiftManager.create(serverLevel, center);
            }
        }

        return InteractionResult.SUCCESS;
    }
}


