package com.example.magicwand;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SniperRifleItem extends Item {

    private static final double RANGE = 100.0;

    // Урон «с одного выстрела». Броня режет урон максимум на 80%,
    // поэтому 1000 убивает практически любого (в том числе игрока в незеритовой броне).
    private static final float DAMAGE = 1000.0F;

    // Затвор: весь цикл (выстрел, открыть, закрыть) занимает 2 секунды
    private static final int COOLDOWN_TICKS = 40;
    private static final int BOLT_OPEN_TICK = 10;   // затвор открывается через 0.5 с
    private static final int BOLT_CLOSE_TICK = 26;  // и закрывается через 1.3 с

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

            // Затвор закрыт на момент выстрела
            SniperBolt.setBoltOpen(player, false);

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

            // Пуля пробивает насквозь: бьём ВСЕХ, кто стоит на линии выстрела
            AABB box = player.getBoundingBox()
                    .expandTowards(dir.scale(RANGE))
                    .inflate(1.0);

            List<Entity> targets = level.getEntities(
                    player, box,
                    e -> !e.isSpectator() && e.isPickable()
            );

            for (Entity target : targets) {
                if (target.getBoundingBox().inflate(0.3).clip(start, limit).isPresent()) {
                    target.hurtServer(
                            serverLevel,
                            serverLevel.damageSources().playerAttack(player),
                            DAMAGE
                    );
                }
            }

            // След выстрела из частиц
            for (double d = 1.5; d < start.distanceTo(limit); d += 1.0) {
                Vec3 p = start.add(dir.scale(d));
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        p.x, p.y, p.z, 1, 0, 0, 0, 0
                );
            }

            // Звук выстрела (громкость 3.0 слышна примерно на 48 блоков)
            level.playSound(null, player.blockPosition(),
                    ModSounds.SNIPER_SHOT, SoundSource.PLAYERS, 3.0F, 1.0F);

            player.getCooldowns().addCooldown(
                    player.getItemInHand(hand), COOLDOWN_TICKS
            );

            // Тратим один патрон
            if (!creative) {
                ammo.shrink(1);
            }

            // Передёргивание затвора: звук и смена модели
            if (player instanceof ServerPlayer shooter) {
                SniperBolt.schedule(shooter, BOLT_OPEN_TICK, p -> {
                    SniperBolt.setBoltOpen(p, true);
                    p.level().playSound(null, p.blockPosition(),
                            ModSounds.SNIPER_BOLT_OPEN, SoundSource.PLAYERS, 1.0F, 1.0F);
                });
                SniperBolt.schedule(shooter, BOLT_CLOSE_TICK, p -> {
                    SniperBolt.setBoltOpen(p, false);
                    p.level().playSound(null, p.blockPosition(),
                            ModSounds.SNIPER_BOLT_CLOSE, SoundSource.PLAYERS, 1.0F, 1.0F);
                });
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