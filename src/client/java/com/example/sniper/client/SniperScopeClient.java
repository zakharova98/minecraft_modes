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

    /** Радиус прицела (в GUI-пикселях). Всё остальное считается от него. */
    private static final int SCOPE_RADIUS = 40;
    private static final int RING_WIDTH = 2;

    private static final int MASK_COLOR = 0xFF000000;
    private static final int RING_COLOR = 0xFF111111;
    private static final int THICK_COLOR = 0xFF111111;
    private static final int THIN_COLOR = 0xFF222222;

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

    /** Полуширина круга радиуса r на строке со смещением dy от центра. */
    private static int halfWidth(int r, int dy) {
        if (Math.abs(dy) >= r) return 0;
        return (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (!isAiming(mc)) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;
        int r = SCOPE_RADIUS;

        // 1. Чёрная маска с круглым окном радиуса r
        for (int y = 0; y < h; y++) {
            int dy = y - cy;
            if (Math.abs(dy) > r) {
                graphics.fill(0, y, w, y + 1, MASK_COLOR);
            } else {
                int dx = halfWidth(r, dy);
                graphics.fill(0, y, cx - dx, y + 1, MASK_COLOR);
                graphics.fill(cx + dx, y, w, y + 1, MASK_COLOR);
            }
        }

        // 2. Обод линзы (кольцо толщиной RING_WIDTH внутри круга)
        int innerR = r - RING_WIDTH;
        for (int y = cy - r; y <= cy + r; y++) {
            int dy = y - cy;
            int outer = halfWidth(r, dy);
            if (outer == 0) continue;
            int inner = halfWidth(innerR, dy);
            if (inner == 0) {
                graphics.fill(cx - outer, y, cx + outer, y + 1, RING_COLOR);
            } else {
                graphics.fill(cx - outer, y, cx - inner, y + 1, RING_COLOR);
                graphics.fill(cx + inner, y, cx + outer, y + 1, RING_COLOR);
            }
        }

        // Размеры элементов прицела пропорциональны радиусу
        int thick = 2;
        int thickLen = r * 3 / 8;   // 15 при r = 40
        int gap = Math.max(2, r / 10); // 4 при r = 40
        int tickLen = 4;
        int tickGap = Math.max(4, r / 5); // 8 при r = 40

        // 3. Толстые линии по краям
        graphics.fill(cx - r, cy - thick / 2, cx - r + thickLen, cy + thick / 2, THICK_COLOR);
        graphics.fill(cx + r - thickLen, cy - thick / 2, cx + r, cy + thick / 2, THICK_COLOR);
        graphics.fill(cx - thick / 2, cy - r, cx + thick / 2, cy - r + thickLen, THICK_COLOR);
        graphics.fill(cx - thick / 2, cy + r - thickLen, cx + thick / 2, cy + r, THICK_COLOR);

        // 4. Тонкие линии к центру
        graphics.fill(cx - r + thickLen, cy, cx - gap, cy + 1, THIN_COLOR);
        graphics.fill(cx + gap, cy, cx + r - thickLen, cy + 1, THIN_COLOR);
        graphics.fill(cx, cy - r + thickLen, cx + 1, cy - gap, THIN_COLOR);
        graphics.fill(cx, cy + gap, cx + 1, cy + r - thickLen, THIN_COLOR);

        // 5. Штрихи-деления
        for (int i = -r + thickLen; i <= r - thickLen; i += tickGap) {
            if (i == 0) continue;
            graphics.fill(cx + i, cy - tickLen / 2, cx + i + 1, cy + tickLen / 2, THIN_COLOR);
            graphics.fill(cx - tickLen / 2, cy + i, cx + tickLen / 2, cy + i + 1, THIN_COLOR);
        }

        // 6. Красная точка в центре
        graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFF0000);

        // 7. Блик линзы
        int glintColor = 0x33FFFFFF;
        int g = r * 3 / 8; // 15 при r = 40
        graphics.fill(cx + g, cy + g, cx + g + 10, cy + g + 2, glintColor);
        graphics.fill(cx + g + 2, cy + g - 2, cx + g + 12, cy + g, glintColor);
    }
}