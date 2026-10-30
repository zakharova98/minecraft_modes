
package com.example.voidmod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class VoidBoltManager {

    private static final double SPEED = 1.25;
    private static final int MAX_AGE = 40;
    private static final float DAMAGE = 8.0F;

    private static final Map<ServerLevel, List<VoidBolt>> BOLTS =
            new HashMap<>();

    private VoidBoltManager() {
    }

    public static void create(ServerLevel level, Player owner) {
        Vec3 direction = owner.getLookAngle().normalize();

        Vec3 start = owner.getEyePosition()
                .add(direction.scale(0.8));

        VoidBolt bolt = new VoidBolt(
                owner,
                start,
                direction.scale(SPEED)
        );

        BOLTS.computeIfAbsent(level, key -> new ArrayList<>())
                .add(bolt);

        // Фиолетовая вспышка при запуске.
        level.sendParticles(
                ParticleTypes.PORTAL,
                start.x, start.y, start.z,
                20,
                0.12, 0.12, 0.12,
                0.04
        );

        // Тёмное облако в момент появления.
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                start.x, start.y, start.z,
                6,
                0.06, 0.06, 0.06,
                0.015
        );

        TestingMod1.LOGGER.info("Снаряд Пустоты запущен!");
    }

    public static void tick(ServerLevel level) {
        List<VoidBolt> bolts = BOLTS.get(level);

        if (bolts == null) {
            return;
        }

        Iterator<VoidBolt> iterator = bolts.iterator();

        while (iterator.hasNext()) {
            VoidBolt bolt = iterator.next();
            bolt.age++;

            Vec3 start = bolt.position;
            Vec3 end = start.add(bolt.velocity);

            // Проверяем столкновение с блоками.
            BlockHitResult blockHit = level.clip(
                    new ClipContext(
                            start,
                            end,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            bolt.owner
                    )
            );

            double blockDistance = Double.POSITIVE_INFINITY;

            if (blockHit.getType() != HitResult.Type.MISS) {
                blockDistance = start.distanceTo(
                        blockHit.getLocation()
                );
            }

            // Ищем мобов на пути снаряда.
            AABB searchArea = new AABB(start, end).inflate(0.6);

            List<LivingEntity> targets =
                    level.getEntitiesOfClass(
                            LivingEntity.class,
                            searchArea,
                            entity -> entity.isAlive()
                                    && entity != bolt.owner
                    );

            LivingEntity hitTarget = null;
            double nearestDistance = Double.POSITIVE_INFINITY;

            Vec3 movement = end.subtract(start);
            double movementLengthSquared = movement.lengthSqr();

            for (LivingEntity target : targets) {
                Vec3 center = target.position().add(
                        0.0,
                        target.getBbHeight() * 0.5,
                        0.0
                );

                Vec3 relative = center.subtract(start);

                double t = relative.dot(movement)
                        / movementLengthSquared;

                t = Math.max(0.0, Math.min(1.0, t));

                Vec3 closestPoint = start.add(movement.scale(t));

                double hitRadius = target.getBbWidth() * 0.5 + 0.35;

                if (closestPoint.distanceTo(center) <= hitRadius) {
                    double distance = start.distanceTo(closestPoint);

                    if (distance < nearestDistance
                            && distance < blockDistance) {
                        nearestDistance = distance;
                        hitTarget = target;
                    }
                }
            }

            // Попадание в моба.
            if (hitTarget != null) {
                hitTarget.hurtServer(
                        level,
                        level.damageSources().magic(),
                        DAMAGE
                );

                Vec3 knockback = bolt.velocity.normalize()
                        .scale(0.65);

                hitTarget.push(
                        knockback.x,
                        0.25,
                        knockback.z
                );

                createImpact(level, hitTarget.position());

                iterator.remove();
                continue;
            }

            // Попадание в стену.
            if (blockHit.getType() != HitResult.Type.MISS) {
                createImpact(level, blockHit.getLocation());

                iterator.remove();
                continue;
            }

            // Передвигаем снаряд.
            bolt.position = end;

            // Рисуем ядро, ауру и вращающиеся кольца.
            renderBolt(level, end, bolt.age, bolt.velocity);

            // Удаляем снаряд после окончания полёта.
            if (bolt.age >= MAX_AGE) {
                createImpact(level, bolt.position);
                iterator.remove();
            }
        }

        if (bolts.isEmpty()) {
            BOLTS.remove(level);
        }
    }

    private static void renderBolt(
            ServerLevel level,
            Vec3 position,
            int age,
            Vec3 velocity
    ) {
        // 1. Тёмное ядро.
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                position.x, position.y, position.z,
                5,
                0.035, 0.035, 0.035,
                0.005
        );

        // 2. Фиолетовая аура.
        level.sendParticles(
                ParticleTypes.PORTAL,
                position.x, position.y, position.z,
                10,
                0.10, 0.10, 0.10,
                0.015
        );

        // 3. Строим плоскость колец,
        // ориентированную по направлению полёта.
        Vec3 forward = velocity.normalize();

        Vec3 reference;

        if (Math.abs(forward.y) < 0.9) {
            reference = new Vec3(0.0, 1.0, 0.0);
        } else {
            reference = new Vec3(1.0, 0.0, 0.0);
        }

        Vec3 right = forward.cross(reference).normalize();
        Vec3 up = forward.cross(right).normalize();

        // 4. Два визуальных кольца с вращением.
        double angleOffset = age * 0.42;
        double radius = 0.23;

        for (int i = 0; i < 12; i++) {
            double angle = angleOffset
                    + 2.0 * Math.PI * i / 12.0;

            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            Vec3 ringOffset = right.scale(x)
                    .add(up.scale(y));

            Vec3 ringPosition = position.add(ringOffset);

            // Основное фиолетовое кольцо.
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    ringPosition.x,
                    ringPosition.y,
                    ringPosition.z,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );

            // Второе кольцо, вращающееся в другую сторону.
            double secondAngle = -angleOffset
                    + 2.0 * Math.PI * i / 12.0;

            Vec3 secondOffset = right.scale(
                    Math.cos(secondAngle) * radius * 0.72
            ).add(
                    up.scale(
                            Math.sin(secondAngle) * radius * 0.72
                    )
            );

            Vec3 secondPosition = position.add(secondOffset);

            level.sendParticles(
                    ParticleTypes.PORTAL,
                    secondPosition.x,
                    secondPosition.y,
                    secondPosition.z,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }

        // 5. Тёмный след позади снаряда.
        Vec3 backward = position.subtract(
                forward.scale(0.20)
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                backward.x,
                backward.y,
                backward.z,
                3,
                0.025, 0.025, 0.025,
                0.005
        );
    }

    private static void createImpact(
            ServerLevel level,
            Vec3 position
    ) {
        // Большая вспышка при столкновении.
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                position.x, position.y, position.z,
                1,
                0.0, 0.0, 0.0,
                0.0
        );

        // Мощный фиолетовый выброс.
        level.sendParticles(
                ParticleTypes.PORTAL,
                position.x, position.y, position.z,
                70,
                0.45, 0.40, 0.45,
                0.18
        );

        // Тёмное облако энергии.
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                position.x, position.y, position.z,
                24,
                0.30, 0.30, 0.30,
                0.06
        );

        // Светящиеся искры при попадании.
        level.sendParticles(
                ParticleTypes.END_ROD,
                position.x, position.y, position.z,
                16,
                0.25, 0.25, 0.25,
                0.10
        );
    }

    private static final class VoidBolt {

        private final Player owner;
        private final Vec3 velocity;

        private Vec3 position;
        private int age;

        private VoidBolt(
                Player owner,
                Vec3 position,
                Vec3 velocity
        ) {
            this.owner = owner;
            this.position = position;
            this.velocity = velocity;
            this.age = 0;
        }
    }
}


