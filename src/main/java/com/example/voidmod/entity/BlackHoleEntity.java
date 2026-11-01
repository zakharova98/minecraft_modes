package com.example.voidmod.entity;

import com.example.voidmod.ModEntities;

import com.example.voidmod.VoidCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlackHoleEntity extends Entity {

    private static final double GRAVITY_RADIUS = 8.0;
    private static final double GRAVITY_STRENGTH = 0.12;

    private static final double ABSORB_RADIUS = 1.4;

    private static final int MAX_ABSORBED = 20;

    private int absorbedObjects = 0;

    private int age = 0;

    public BlackHoleEntity(
            EntityType<? extends BlackHoleEntity> type,
            Level level
    ) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        // Пока чёрная дыра не использует синхронизируемые данные
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("absorbedObjects", absorbedObjects);
        output.putInt("age", age);
    }

    @Override
    public boolean hurtServer(
            ServerLevel level,
            DamageSource source,
            float amount
    ) {
        // Чёрная дыра пока не получает урон
        return false;
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        absorbedObjects = input.getIntOr("absorbedObjects", 0);
        age = input.getIntOr("age", 0);
    }

    @Override
    public void tick() {
        super.tick();

        age++;

        if (level().isClientSide()) {
            createParticles();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level();

        attractEntities(serverLevel);

        // Поглощаем блоки раз в секунду
        if (age % 20 == 0) {
            absorbBlocks(serverLevel);
        }

        // Проверяем количество поглощённых объектов
        if (absorbedObjects >= MAX_ABSORBED) {
            explode(serverLevel);
        }
    }

    // Визуальные эффекты
    private void createParticles() {

        for (int i = 0; i < 5; i++) {

            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;

            level().addParticle(
                    ParticleTypes.PORTAL,
                    getX() + offsetX,
                    getY() + offsetY,
                    getZ() + offsetZ,
                    -offsetX * 0.1,
                    -offsetY * 0.1,
                    -offsetZ * 0.1
            );
        }

        level().addParticle(
                ParticleTypes.REVERSE_PORTAL,
                getX(),
                getY(),
                getZ(),
                0,
                0,
                0
        );
    }

    // Притяжение сущностей
    private void attractEntities(ServerLevel serverLevel) {

        AABB area = getBoundingBox().inflate(GRAVITY_RADIUS);

        for (Entity entity : level().getEntities(
                this,
                area,
                e -> e.isAlive()
        )) {

            Vec3 direction = new Vec3(
                    getX() - entity.getX(),
                    getY() - entity.getY(),
                    getZ() - entity.getZ()
            );

            double distance = direction.length();

            if (distance < 0.1) {
                continue;
            }

            Vec3 pull = direction.normalize()
                    .scale(GRAVITY_STRENGTH);

            // Притягиваем игроков, мобов и предметы
            entity.setDeltaMovement(
                    entity.getDeltaMovement().add(pull)
            );

            // Поглощение объектов
            if (distance < ABSORB_RADIUS) {

                if (entity instanceof Player) {
                    continue;
                }

                if (entity instanceof ItemEntity ||
                        entity instanceof LivingEntity) {

                    entity.discard();

                    absorbedObjects++;
                }
            }
        }
    }

    // Поглощение блоков около центра
    private void absorbBlocks(ServerLevel serverLevel) {

        BlockPos center = blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-1, -1, -1),
                center.offset(1, 1, 1)
        )) {

            if (pos.equals(center)) {
                continue;
            }

            if (!serverLevel.getBlockState(pos).isAir()) {

                boolean destroyed = serverLevel.destroyBlock(
                        pos,
                        true
                );

                if (destroyed) {
                    absorbedObjects++;
                }
            }
        }
    }

    // Взрыв чёрной дыры
    private void explode(ServerLevel serverLevel) {

        VoidCraft.LOGGER.info(
                "VoidCraft: Чёрная дыра поглотила {} объектов!",
                absorbedObjects
        );

        serverLevel.explode(
                this,
                getX(),
                getY(),
                getZ(),
                4.0F,
                Level.ExplosionInteraction.MOB
        );

        discard();
    }
}