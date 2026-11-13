
package com.example.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class GolemConfiguratorItem extends Item {

    public GolemConfiguratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof ChestBlockEntity)) {
            return InteractionResult.PASS;
        }

        GolemCategory current =
                GolemChestManager.getCategory(serverLevel, pos);

        GolemCategory[] categories = GolemCategory.values();

        int next = 0;

        if (current != null) {
            next = (current.ordinal() + 1) % categories.length;
        }

        GolemCategory category = categories[next];

        GolemChestManager.assign(serverLevel, pos, category);

        if (context.getPlayer() instanceof ServerPlayer player) {
            player.sendSystemMessage(
                    Component.literal("Категория сундука: " + category.getDisplayName())
            );
        }

        return InteractionResult.SUCCESS;
    }

    public static String getChestName(GolemCategory category) {
        return "[GOLEM:" + category.name() + "]";
    }
}
