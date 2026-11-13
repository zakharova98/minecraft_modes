package com.example.golem.client;

import com.example.golem.menu.GolemMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;

public class GolemScreen extends AbstractContainerScreen<GolemMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "golem",
                    "textures/item/golem_inventory.png");

    public GolemScreen(
            GolemMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, 176, 166);
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();

        Button sortButton = Button.builder(
                Component.literal("Сортировать"),
                button -> {
                    if (this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                0
                        );
                    }
                }
        ).bounds(
                this.leftPos + 104,
                this.topPos + 3,
                64,
                16
        ).build();

        this.addRenderableWidget(sortButton);
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                176,
                166,
                176,
                166
        );
    }
}