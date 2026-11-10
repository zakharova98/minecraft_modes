package com.example.magicwand;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer; // <--- ДОБАВЬТЕ ЭТУ СТРОКУ
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SniperRifleItem extends Item {

    private static final double RANGE = 100.0;
    private static final float DAMAGE = 20.0F;
    private static final int COOLDOWN_TICKS = 40; // 2 секунды

    public SniperRifleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {

            // Проверяем патроны (в творческом режиме они не нужны)
            boolean creative = player.getAbilities().instabuild;
            ItemStack ammo = findAmmo(player);

            if (!creative && ammo.isEmpty()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendOverlayMessage(Component.literal("Нет патронов!"));
                }
                level.playSound(null, player.blockPosition(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.FAIL;
            }

            Vec3 start = player.getEyePosition();
            Vec3 dir = player.getLookAngle();
            Vec3 end = start.add(dir.scale(RANGE));

            // Луч останавливается на первом блоке
            BlockHitResult blockHit = level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            Vec3 limit = blockHit.getType() == HitResult.Type.MISS
                    ? end
                    : blockHit.getLocation();

            // Ищем сущность на пути луча
            AABB box = player.getBoundingBox()
                    .expandTowards(dir.scale(RANGE))
                    .inflate(1.0);

            EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                    player, start, limit, box,
                    e -> !e.isSpectator() && e.isPickable(),
                    RANGE * RANGE
            );

            if (hit != null) {
                hit.getEntity().hurtServer(
                        serverLevel,
                        serverLevel.damageSources().playerAttack(player),
                        DAMAGE
                );
            }

            // След выстрела из частиц
            for (double d = 1.5; d < start.distanceTo(limit); d += 1.0) {
                Vec3 p = start.add(dir.scale(d));
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        p.x, p.y, p.z, 1, 0, 0, 0, 0
                );
            }

            level.playSound(null, player.blockPosition(),
                    SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.PLAYERS, 2.0F, 0.6F);

            player.getCooldowns().addCooldown(
                    player.getItemInHand(hand), COOLDOWN_TICKS
            );

            // Тратим один патрон
            if (!creative) {
                ammo.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    // Ищем патроны в инвентаре
    private static ItemStack findAmmo(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(MagicWandMod.SNIPER_AMMO)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}