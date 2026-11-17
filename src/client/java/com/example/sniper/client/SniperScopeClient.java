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
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

public class SniperScopeClient {

    private static final int ZOOM_FOV = 30;
    private static boolean zooming = false;
    private static int savedFov = 70;

    // Путь к нашей текстуре
    private static final Identifier SCOPE_TEXTURE =
            Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "textures/gui/sniper_scope.png");

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
                Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "sniper_scope"),
                SniperScopeClient::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (!isAiming(mc)) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // В версии 26.3 аргументы blit идут в таком порядке:
        // (RenderPipeline, Identifier, x, y, u, v, width, height, texWidth, texHeight)
        // Мы рисуем текстуру на весь экран. Так как она квадратная, она растянется,
        // но черные края из текстуры перекроют экран, а в центре останется круг.
        graphics.blit(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                SCOPE_TEXTURE,
                0, 0,                 // x, y
                0, 0,                 // u, v
                w, h,                 // width, height (размер на экране)
                w, h                  // texWidth, texHeight (размер текстуры)
        );
    }
}