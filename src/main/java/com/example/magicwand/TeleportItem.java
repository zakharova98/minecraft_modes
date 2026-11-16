package com.example.magicwand;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TeleportItem extends Item {

    public TeleportItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {

            ItemStack stack = player.getItemInHand(hand);

            // Если игрок зажал Shift - СОХРАНЯЕМ позицию
            if (player.isShiftKeyDown()) {
                // Используем toString() для получения строки измерения
                String dimensionName = player.level().dimension().toString();

                TeleportData data = new TeleportData(
                        player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot(),
                        dimensionName
                );

                // Сохраняем данные в предмет
                stack.set(MagicWandMod.TELEPORT_DATA, data);

                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aПозиция успешно сохранена!"));
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);

            } else {
                // Если обычный клик - ТЕЛЕПОРТИРУЕМ
                TeleportData data = stack.get(MagicWandMod.TELEPORT_DATA);

                if (data != null) {
                    // Сравниваем строки измерений
                    if (!player.level().dimension().toString().equals(data.dimension())) {
                        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cВы должны находиться в том же измерении!"));
                        return InteractionResult.FAIL;
                    }

                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 0.5, 0.5, 0.2);

                    // Телепортация (исправленная сигнатура под 1.21.5+)
                    serverPlayer.teleportTo(serverLevel, data.x(), data.y(), data.z(), java.util.Set.of(), data.yaw(), data.pitch(), false);

                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
                    serverLevel.sendParticles(ParticleTypes.PORTAL, data.x(), data.y() + 1, data.z(), 50, 0.5, 0.5, 0.5, 0.2);

                } else {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cТочка сохранения не найдена! Зажмите Shift + ПКМ, чтобы сохранить."));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}