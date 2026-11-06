package com.example.unicorn;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class UnicornEntity extends Horse {

    private static final EntityDataAccessor<Boolean> MAGICAL_SPRINT =
            SynchedEntityData.defineId(
                    UnicornEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    public UnicornEntity(
            EntityType<? extends UnicornEntity> type,
            Level level
    ) {
        super(type, level);
    }

    // Регистрируем синхронизируемое состояние
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MAGICAL_SPRINT, false);
    }

    // Включение и выключение ускорения
    public void setMagicalSprint(boolean active) {
        this.entityData.set(MAGICAL_SPRINT, active);

        // Включаем или выключаем светящийся контур
        this.setGlowingTag(active);
    }

    public boolean isMagicalSprint() {
        return this.entityData.get(MAGICAL_SPRINT);
    }

    // Увеличение скорости
    @Override
    protected float getRiddenSpeed(Player controller) {

        float normalSpeed = super.getRiddenSpeed(controller);

        if (isMagicalSprint()) {
            return normalSpeed * 1.7F;
        }

        return normalSpeed;
    }

    // Магические частицы
    @Override
    public void tick() {
        super.tick();

        // Выключаем ускорение, если игрок слез
        if (!level().isClientSide()
                && getControllingPassenger() == null
                && isMagicalSprint()) {

            setMagicalSprint(false);
        }

        // Создаём частицы только на сервере
        if (level() instanceof ServerLevel serverLevel
                && isMagicalSprint()
                && tickCount % 3 == 0
                && getDeltaMovement().horizontalDistanceSqr() > 0.0025) {

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    getX(),
                    getY() + 0.15,
                    getZ(),
                    5,
                    0.3,
                    0.05,
                    0.3,
                    0.02
            );
        }
    }
}