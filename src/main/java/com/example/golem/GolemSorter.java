
package com.example.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class GolemSorter {

    private static final int RADIUS = 12;

    public static int sortGolemInventory(GolemEntity golem) {
        if (!(golem.level() instanceof ServerLevel level)) {
            return 0;
        }

        Container storage = golem.getStorage();

        List<ChestBlockEntity> chests = findChests(
                level,
                golem.blockPosition()
        );

        int totalMoved = 0;

        for (int i = 0; i < storage.getContainerSize(); i++) {
            ItemStack source = storage.getItem(i);

            if (source.isEmpty()) {
                continue;
            }

            GolemCategory category = getCategory(source);

            for (ChestBlockEntity chest : chests) {
                if (!hasCategory(level, chest, category)) {
                    continue;
                }

                for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                    if (!chest.getItem(slot).isEmpty()) {
                        continue;
                    }

                    int amount = Math.min(
                            source.getCount(),
                            source.getMaxStackSize()
                    );

                    ItemStack placed = source.copy();
                    placed.setCount(amount);

                    chest.setItem(slot, placed);
                    chest.setChanged();

                    source.shrink(amount);
                    totalMoved += amount;

                    if (source.isEmpty()) {
                        break;
                    }
                }

                if (source.isEmpty()) {
                    break;
                }
            }

            storage.setItem(i, source);
        }

        storage.setChanged();

        return totalMoved;
    }

    private static List<ChestBlockEntity> findChests(
            ServerLevel level,
            BlockPos center
    ) {
        List<ChestBlockEntity> result = new ArrayList<>();

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {

                    BlockPos pos = center.offset(x, y, z);

                    if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {

                        if (GolemChestManager.getCategory(level, pos) != null) {
                            result.add(chest);
                        }
                    }
                }
            }
        }

        return result;
    }

    private static boolean hasCategory(
            ServerLevel level,
            ChestBlockEntity chest,
            GolemCategory category
    ) {
        return GolemChestManager.getCategory(
                level,
                chest.getBlockPos()
        ) == category;
    }

    private static int putIntoEmptySlots(
            Container chest,
            ItemStack source
    ) {

        int moved = 0;

        for (int slot = 0;
             slot < chest.getContainerSize() && !source.isEmpty();
             slot++) {

            if (!chest.getItem(slot).isEmpty()) {
                continue;
            }

            int amount = Math.min(
                    source.getCount(),
                    source.getMaxStackSize()
            );

            ItemStack copy = source.copy();
            copy.setCount(amount);

            chest.setItem(slot, copy);

            source.shrink(amount);
            moved += amount;
        }

        chest.setChanged();

        return moved;
    }

    // Определение категории предмета
    public static GolemCategory getCategory(ItemStack stack) {
        return GolemCategoryClassifier.getCategory(stack);
    }


}
