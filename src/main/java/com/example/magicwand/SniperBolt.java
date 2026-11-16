package com.example.magicwand;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

/**
 * Затвор снайперки: отложенные действия (звуки, смена модели) через несколько тиков после выстрела.
 */
public class SniperBolt {

    private static class Task {
        final UUID player;
        int ticksLeft;
        final Consumer<ServerPlayer> action;

        Task(UUID player, int ticksLeft, Consumer<ServerPlayer> action) {
            this.player = player;
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }

    private static final List<Task> TASKS = new ArrayList<>();

    // Выполнить действие через delayTicks тиков
    public static void schedule(ServerPlayer player, int delayTicks, Consumer<ServerPlayer> action) {
        TASKS.add(new Task(player.getUUID(), delayTicks, action));
    }

    // Вызывается каждый серверный тик (см. MagicWandMod)
    public static void tick(MinecraftServer server) {
        if (TASKS.isEmpty()) {
            return;
        }

        List<Task> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                due.add(task);
                it.remove();
            }
        }

        for (Task task : due) {
            ServerPlayer player = server.getPlayerList().getPlayer(task.player);
            if (player != null) {
                task.action.accept(player);
            }
        }
    }

    // Показывает модель с открытым затвором (true) или с закрытым (false) у всех винтовок игрока
    public static void setBoltOpen(Player player, boolean open) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(MagicWandMod.SNIPER_RIFLE)) {
                if (open) {
                    stack.set(
                            DataComponents.CUSTOM_MODEL_DATA,
                            new CustomModelData(
                                    List.of(1.0F),   // ← вот тут 1.0F вместо true
                                    List.of(),
                                    List.of(),
                                    List.of()
                            )
                    );
                } else {
                    stack.remove(DataComponents.CUSTOM_MODEL_DATA);
                }
            }
        }
    }
}
