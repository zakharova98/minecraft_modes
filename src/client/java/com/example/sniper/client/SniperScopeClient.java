package com.example.sniper.client;

import com.example.magicwand.MagicWandMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.InteractionResult;

public class SniperScopeClient {

    private static final int ZOOM_FOV = 30;
    private static boolean zooming = false;
    private static int savedFov = 70;

    private static boolean isAiming(Minecraft mc) {
        return mc.player != null
                && mc.options.getCameraType().isFirstPerson()
                && mc.options.keyAttack.isDown()
                && mc.player.getMainHandItem().is(MagicWandMod.SNIPER_RIFLE);
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean aiming = isAiming(client);
            if (aiming && !zooming) {
                savedFov = client.options.fov().get();
                client.options.fov().set(ZOOM_FOV);
                zooming = true;
            } else if (!aiming && zooming) {
                client.options.fov().set(savedFov);
                zooming = false;
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (zooming) {
                client.options.fov().set(savedFov);
                zooming = false;
            }
        });

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                player.getMainHandItem().is(MagicWandMod.SNIPER_RIFLE) ? InteractionResult.FAIL : InteractionResult.PASS);

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                player.getMainHandItem().is(MagicWandMod.SNIPER_RIFLE) ? InteractionResult.FAIL : InteractionResult.PASS);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "sniper_scope"),
                SniperScopeClient::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (!isAiming(mc)) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;

        // 1. ЧЕРНАЯ МАСКА С КРУГОМ (радиус 40)
        int radius = 40;
        int maskColor = 0xFF000000;

        for (int y = 0; y < h; y++) {
            int dy = y - cy;
            if (Math.abs(dy) > radius) {
                graphics.fill(0, y, w, y + 1, maskColor);
            } else {
                int dx = (int) Math.sqrt(radius * radius - dy * dy);
                graphics.fill(0, y, cx - dx, y + 1, maskColor);
                graphics.fill(cx + dx, y, w, y + 1, maskColor);
            }
        }

        // 2. РИСУЕМ ПРИЦЕЛ (РАДИУС 40)
        int colorLine = 0xFF000000;
        int thick = 2;
        int thickLen = 15;
        int gap = 4;

        // Толстые линии
        graphics.fill(cx - radius, cy - thick/2, cx - radius + thickLen, cy + thick/2, colorLine);
        graphics.fill(cx + radius - thickLen, cy - thick/2, cx + radius, cy + thick/2, colorLine);
        graphics.fill(cx - thick/2, cy - radius, cx + thick/2, cy - radius + thickLen, colorLine);
        graphics.fill(cx - thick/2, cy + radius - thickLen, cx + thick/2, cy + radius, colorLine);

        // Тонкие линии
        int thinColor = 0xFF222222;
        graphics.fill(cx - radius + thickLen, cy, cx - gap, cy + 1, thinColor);
        graphics.fill(cx + gap, cy, cx + radius - thickLen, cy + 1, thinColor);
        graphics.fill(cx, cy - radius + thickLen, cx + 1, cy - gap, thinColor);
        graphics.fill(cx, cy + gap, cx + 1, cy + radius - thickLen, thinColor);

        // Штрихи
        int tickLen = 4;
        int tickGap = 8;
        for (int i = -radius + thickLen; i <= radius - thickLen; i += tickGap) {
            if (i == 0) continue;
            graphics.fill(cx + i, cy - tickLen/2, cx + i + 1, cy + tickLen/2, thinColor);
            graphics.fill(cx - tickLen/2, cy + i, cx + tickLen/2, cy + i + 1, thinColor);
        }

        // Красная точка
        graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFF0000);

        // Блик линзы
        int glintColor = 0x33FFFFFF;
        graphics.fill(cx + 15, cy + 15, cx + 25, cy + 17, glintColor);
        graphics.fill(cx + 17, cy + 13, cx + 27, cy + 15, glintColor);
    }
}
