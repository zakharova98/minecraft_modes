
package com.example.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import com.example.golem.menu.GolemMenu;
import net.minecraft.server.level.ServerPlayer;

public class GolemEntity extends IronGolem implements MenuProvider {


    public GolemEntity(
            EntityType<? extends IronGolem> type,
            Level level
    ) {
        super(type, level);
    }

    private final SimpleContainer storage = new SimpleContainer(27) {
        @Override
        public void setChanged() {
            super.setChanged();
            GolemEntity.this.setPersistenceRequired();
        }
    };

    public Container getStorage() {
        return storage;
    }

    // Пока отключаем боевое поведение обычного железного голема.
    @Override
    protected void registerGoals() {
        // Позже добавим собственные AI Goals:
        // поиск сундуков, перенос предметов и сортировку.
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
        }

        return InteractionResult.SUCCESS;
    }

    public Component getDisplayName() {
        return Component.literal("Голем-кладовщик");
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
    ) {
        return new GolemMenu(
                containerId,
                inventory,
                storage,
                this
        );
    }
}
