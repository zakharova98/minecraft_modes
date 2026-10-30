
package com.example.voidmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VoidSphereItem extends Item {

    public VoidSphereItem(Properties properties) {
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

            Vec3 start = user.getEyePosition();

            Vec3 end = start.add(
                    user.getLookAngle().scale(8.0)
            );

            // Ищем место, куда направлен взгляд игрока.
            BlockHitResult hit = level.clip(
                    new ClipContext(
                            start,
                            end,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            user
                    )
            );

            Vec3 center;

            if (hit.getType() != HitResult.Type.MISS) {
                center = hit.getLocation();
            } else {
                center = end;
            }

            VoidBlackHoleManager.create(
                    serverLevel,
                    user,
                    center
            );
        }

        return InteractionResult.SUCCESS;
    }
}
