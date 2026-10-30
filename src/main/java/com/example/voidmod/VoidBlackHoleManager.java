
package com.example.voidmod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class VoidBlackHoleManager {

    private static final double RADIUS = 5.5;
    private static final int DURATION = 100;
    private static final int DAMAGE_INTERVAL = 10;
    private static final float DAMAGE = 2.0F;

    private static final Map<ServerLevel, List<VoidBlackHole>> HOLES =
            new HashMap<>();

    private VoidBlackHoleManager() {
    }

    public static void create(
            ServerLevel level,
            Player owner,
            Vec3 center
    ) {
        VoidBlackHole hole = new VoidBlackHole(owner, center);

        HOLES.computeIfAbsent(level, key -> new ArrayList<>())
                .add(hole);

        // Вспышка при создании чёрной дыры.
        level.sendParticles(
                ParticleTypes.PORTAL,
                center.x, center.y, center.z,
                35,
                0.35, 0.35, 0.35,
                0.12
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                center.x, center.y, center.z,
                12,
                0.2, 0.2, 0.2,
                0.035
        );

        TestingMod1.LOGGER.info("Чёрная дыра Пустоты создана!");
    }

    public static void tick(ServerLevel level) {
        List<VoidBlackHole> holes = HOLES.get(level);

        if (holes == null) {
            return;
        }

        Iterator<VoidBlackHole> iterator = holes.iterator();

        while (iterator.hasNext()) {
            VoidBlackHole hole = iterator.next();

            hole.age++;

            renderHole(level, hole);

            AABB area = new AABB(
                    hole.center.x - RADIUS,
                    hole.center.y - RADIUS,
                    hole.center.z - RADIUS,
                    hole.center.x + RADIUS,
                    hole.center.y + RADIUS,
                    hole.center.z + RADIUS
            );

            List<LivingEntity> targets =
                    level.getEntitiesOfClass(
                            LivingEntity.class,
                            area,
                            entity -> entity.isAlive()
                                    && !(entity instanceof Player)
                    );

            for (LivingEntity target : targets) {
                Vec3 targetCenter = target.position().add(
                        0.0,
                        target.getBbHeight() * 0.5,
                        0.0
                );

                Vec3 difference = hole.center.subtract(targetCenter);
                double distance = difference.length();

                if (distance > RADIUS || distance < 0.001) {
                    continue;
                }

                // Притягиваем моба к центру.
                Vec3 direction = difference.normalize();
                Vec3 current = target.getDeltaMovement();

                double pullStrength = 0.16;
                double liftStrength = distance > 1.2 ? 0.065 : 0.025;

                double vx = current.x * 0.55
                        + direction.x * pullStrength;

                double vy = current.y * 0.55
                        + direction.y * pullStrength
                        + liftStrength;

                double vz = current.z * 0.55
                        + direction.z * pullStrength;

                // Ограничиваем скорость подъёма.
                vy = Math.max(-0.15, Math.min(0.22, vy));

                target.setDeltaMovement(
                        new Vec3(vx, vy, vz)
                );

                // Периодический магический урон.
                if (hole.age % DAMAGE_INTERVAL == 0) {
                    target.hurtServer(
                            level,
                            level.damageSources().magic(),
                            DAMAGE
                    );
                }
            }

            // Завершаем действие через 5 секунд.
            if (hole.age >= DURATION) {
                createCollapse(level, hole.center);
                iterator.remove();
            }
        }

        if (holes.isEmpty()) {
            HOLES.remove(level);
        }
    }

    private static void renderHole(
            ServerLevel level,
            VoidBlackHole hole
    ) {
        Vec3 center = hole.center;

        // Тёмное облако в центре.
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                center.x, center.y, center.z,
                4,
                0.12, 0.12, 0.12,
                0.01
        );

        // Фиолетовая аура.
        level.sendParticles(
                ParticleTypes.PORTAL,
                center.x, center.y, center.z,
                8,
                0.22, 0.22, 0.22,
                0.025
        );

        // Вращающееся кольцо.
        double rotation = hole.age * 0.18;
        double ringRadius = 1.1;

        for (int i = 0; i < 12; i++) {
            double angle = rotation
                    + 2.0 * Math.PI * i / 12.0;

            double x = Math.cos(angle) * ringRadius;
            double z = Math.sin(angle) * ringRadius;
            double y = Math.sin(angle * 2.0) * 0.25;

            level.sendParticles(
                    ParticleTypes.PORTAL,
                    center.x + x,
                    center.y + y,
                    center.z + z,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }

        // Искры возле аномалии.
        if (hole.age % 3 == 0) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    center.x, center.y, center.z,
                    2,
                    0.18, 0.18, 0.18,
                    0.025
            );
        }
    }

    private static void createCollapse(
            ServerLevel level,
            Vec3 center
    ) {
        // Вспышка при исчезновении.
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                center.x, center.y, center.z,
                1,
                0.0, 0.0, 0.0,
                0.0
        );

        level.sendParticles(
                ParticleTypes.PORTAL,
                center.x, center.y, center.z,
                55,
                0.4, 0.4, 0.4,
                0.15
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                center.x, center.y, center.z,
                18,
                0.3, 0.3, 0.3,
                0.05
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x, center.y, center.z,
                12,
                0.25, 0.25, 0.25,
                0.08
        );
    }

    private static final class VoidBlackHole {

        private final Player owner;
        private final Vec3 center;
        private int age;

        private VoidBlackHole(Player owner, Vec3 center) {
            this.owner = owner;
            this.center = center;
            this.age = 0;
        }
    }
}
