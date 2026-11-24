package com.example.magicwand;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GrabberItem extends Item {

    public GrabberItem(Properties properties) {
        super(properties);
    }

    // ПКМ «в воздух» с чем-то в руках: поставить то, что несём, туда, куда смотрим
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!Grabber.isCarrying(stack)) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Grabber.placeFromRay(serverPlayer, serverLevel, stack);
        }

        return InteractionResult.SUCCESS;
    }
}