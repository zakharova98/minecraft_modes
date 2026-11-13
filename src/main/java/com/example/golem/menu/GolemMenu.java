package com.example.golem.menu;

import com.example.golem.GolemEntity;
import com.example.golem.GolemSorter;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

public class GolemMenu extends AbstractContainerMenu {

    public static final int GOLEM_SLOTS = 27;

    private static final int PLAYER_INVENTORY_START = GOLEM_SLOTS;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    private final Container container;
    private final GolemEntity golem;

    // Клиентский конструктор
    public GolemMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(GOLEM_SLOTS),
                null
        );
    }

    // Серверный конструктор
    public GolemMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            GolemEntity golem
    ) {
        super(GolemMenus.STORAGE_GOLEM, containerId);

        checkContainerSize(container, GOLEM_SLOTS);

        this.container = container;
        this.golem = golem;

        // 27 слотов голема
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        container,
                        column + row * 9,
                        8 + column * 18,
                        18 + row * 18
                ));
            }
        }

        // 27 слотов инвентаря игрока
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        85 + row * 18
                ));
            }
        }

// 9 слотов быстрого доступа
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    143
            ));
        }

        container.startOpen(playerInventory.player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack empty = ItemStack.EMPTY;

        Slot slot = this.slots.get(slotIndex);

        if (slot == null || !slot.hasItem()) {
            return empty;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < GOLEM_SLOTS) {
            // Из голема в инвентарь игрока
            if (!this.moveItemStackTo(
                    stack,
                    GOLEM_SLOTS,
                    GOLEM_SLOTS + 36,
                    true
            )) {
                return empty;
            }
        } else {
            // Из инвентаря игрока в голема
            if (!this.moveItemStackTo(
                    stack,
                    0,
                    GOLEM_SLOTS,
                    false
            )) {
                return empty;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return golem == null ||
                (golem.isAlive() && player.distanceToSqr(golem) <= 64.0);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != 0 || golem == null) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (!(serverPlayer.level() instanceof ServerLevel)) {
            return false;
        }

        int moved = GolemSorter.sortGolemInventory(golem);

        serverPlayer.sendSystemMessage(
                Component.literal(
                        "Голем переместил предметов: " + moved
                )
        );

        return true;
    }
}