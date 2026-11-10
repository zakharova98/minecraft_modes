package com.example.unicorn.client;

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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

public class SniperScopeClient {

    private static final Identifier SCOPE =
            Identifier.fromNamespaceAndPath(
                    MagicWandMod.MOD_ID,
                    "textures/gui/sniper_scope.png"
            );

    // Угол обзора при прицеливании (в настройках минимум 30)
    private static final int ZOOM_FOV = 30;

    private static boolean zooming = false;
    private static int savedFov = 70;

    // Прицеливаемся: винтовка в руке, зажата ЛКМ, вид от первого лица
    private static boolean isAiming(Minecraft mc) {
        return mc.player != null
                && mc.options.getCameraType().isFirstPerson()
                && mc.options.keyAttack.isDown()
                && mc.player.getMainHandItem().is(MagicWandMod.SNIPER_RIFLE);
    }

    public static void init() {

        // Приближение: меняем FOV на время прицеливания
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

        // Если закрыли игру во время прицеливания, возвращаем FOV
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (zooming) {
                client.options.fov().set(savedFov);
                zooming = false;
            }
        });

        // С винтовкой в руке ЛКМ не бьёт сущностей и не ломает блоки
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                player.getMainHandItem().is(MagicWandMod.SNIPER_RIFLE)
                        ? InteractionResult.FAIL
                        : InteractionResult.PASS);

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                player.getMainHandItem().is(MagicWandMod.SNIPER_RIFLE)
                        ? InteractionResult.FAIL
                        : InteractionResult.PASS);

        // Рамка прицела
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "sniper_scope"),
                SniperScopeClient::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (!isAiming(mc)) {
            return;
        }

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int size = h;               // квадрат по высоте экрана
        int x = (w - size) / 2;

        // Чёрные полосы по бокам
        graphics.fill(0, 0, x, h, 0xFF000000);
        graphics.fill(x + size, 0, w, h, 0xFF000000);

        // Сам прицел
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, SCOPE,
                x, 0, 0, 0, size, size, size, size
        );
    }
}