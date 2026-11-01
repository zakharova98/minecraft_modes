package com.example.voidmod.item;

import com.example.voidmod.ModEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.example.voidmod.entity.BlackHoleEntity;

public class BlackHoleItem extends Item {

    public BlackHoleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        // На клиенте не создаём сущность
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // Направление взгляда игрока
        Vec3 look = player.getLookAngle();

        // Позиция перед игроком
        Vec3 spawnPosition = player.getEyePosition()
                .add(look.scale(3.0));

        // Создаём чёрную дыру
        BlackHoleEntity blackHole = new BlackHoleEntity(
                ModEntities.BLACK_HOLE,
                level
        );

        blackHole.setPos(
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z
        );

        // Добавляем её в мир
        boolean spawned = level.addFreshEntity(blackHole);

        // Расходуем предмет, если сущность успешно создана
        if (spawned && !player.getAbilities().instabuild) {
            ItemStack stack = player.getItemInHand(hand);
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}