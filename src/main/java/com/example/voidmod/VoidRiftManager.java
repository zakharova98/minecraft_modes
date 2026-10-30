
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

public final class VoidRiftManager {

    private static final int DURATION = 60;
    private static final double MAX_RADIUS = 4.0;

    private static final Map<ServerLevel, List<Rift>> RIFTS =
            new HashMap<>();

    private VoidRiftManager() {
    }

    public static void create(ServerLevel level, Vec3 center) {
        RIFTS.computeIfAbsent(level, key -> new ArrayList<>())
                .add(new Rift(center));

        TestingMod1.LOGGER.info("Черная дыра была открыта!");
    }

    public static void tick(ServerLevel level) {
        List<Rift> rifts = RIFTS.get(level);

        if (rifts == null) {
            return;
        }

        Iterator<Rift> iterator = rifts.iterator();

        while (iterator.hasNext()) {
            Rift rift = iterator.next();

            rift.age++;

            double pulse = 0.8
                    + 0.25 * Math.sin(rift.age * 0.3);

            double radius = pulse;

            // Вращающееся кольцо частиц
            for (int i = 0; i < 16; i++) {
                double angle =
                        rift.age * 0.12
                                + 2.0 * Math.PI * i / 16.0;

                double x = rift.center.x
                        + Math.cos(angle) * radius;

                double z = rift.center.z
                        + Math.sin(angle) * radius;

                double y = rift.center.y
                        + Math.sin(angle * 2.0) * 0.15;

                level.sendParticles(
                        ParticleTypes.PORTAL,
                        x, y, z,
                        1,
                        0.0, 0.08, 0.0,
                        0.02
                );
            }

            // Притягиваем живых существ к центру.
            // Игроков не затрагиваем.
            AABB area = new AABB(
                    rift.center.x - MAX_RADIUS,
                    rift.center.y - MAX_RADIUS,
                    rift.center.z - MAX_RADIUS,
                    rift.center.x + MAX_RADIUS,
                    rift.center.y + MAX_RADIUS,
                    rift.center.z + MAX_RADIUS
            );

            List<LivingEntity> targets =
                    level.getEntitiesOfClass(
                            LivingEntity.class,
                            area,
                            entity -> !(entity instanceof Player)
                                    && entity.isAlive()
                    );

            for (LivingEntity target : targets) {
                Vec3 difference =
                        rift.center.subtract(target.position());

                double distance = difference.length();

                if (distance > 0.5 && distance < MAX_RADIUS) {
                    Vec3 pull = difference.normalize()
                            .scale(0.08);

                    target.setDeltaMovement(
                            target.getDeltaMovement().add(pull)
                    );
                }
            }

            // Закрываем разлом через 3 секунды.
            if (rift.age >= DURATION) {
                level.sendParticles(
                        ParticleTypes.PORTAL,
                        rift.center.x,
                        rift.center.y,
                        rift.center.z,
                        60,
                        0.8, 0.8, 0.8,
                        0.15
                );

                iterator.remove();

                TestingMod1.LOGGER.info(
                        "Черная дыра закрыта!"
                );
            }
        }

        if (rifts.isEmpty()) {
            RIFTS.remove(level);
        }
    }

    private static final class Rift {
        private final Vec3 center;
        private int age = 0;

        private Rift(Vec3 center) {
            this.center = center;
        }
    }
}

