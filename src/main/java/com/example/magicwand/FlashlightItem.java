package com.example.magicwand;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlashlightItem extends Item {

    private final Supplier<Item> nextState;

    public FlashlightItem(
            Properties properties,
            Supplier<Item> nextState
    ) {
        super(properties);
        this.nextState = nextState;
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (level instanceof ServerLevel) {
            ItemStack newStack = new ItemStack(nextState.get());

            player.setItemInHand(hand, newStack);
        }

        return InteractionResult.SUCCESS;
    }
}