package com.example.unicorn;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

public class UnicornEntity extends Horse {

    public UnicornEntity(
            EntityType<? extends UnicornEntity> type,
            Level level) {
        super(type, level);
    }
}
