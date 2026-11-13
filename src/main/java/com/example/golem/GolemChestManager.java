
package com.example.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class GolemChestManager {

    public static void assign(
            ServerLevel level,
            BlockPos pos,
            GolemCategory category
    ) {
        GolemChestData.get(level.getServer())
                .assign(level, pos, category);
    }

    public static GolemCategory getCategory(
            ServerLevel level,
            BlockPos pos
    ) {
        return GolemChestData.get(level.getServer())
                .getCategory(level, pos);
    }

    public static void remove(
            ServerLevel level,
            BlockPos pos
    ) {
        GolemChestData.get(level.getServer())
                .remove(level, pos);
    }
}
