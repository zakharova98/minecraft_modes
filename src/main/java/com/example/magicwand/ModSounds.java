package com.example.magicwand;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent SNIPER_SHOT = register("sniper_shot");
    public static final SoundEvent SNIPER_BOLT_OPEN = register("sniper_bolt_open");
    public static final SoundEvent SNIPER_BOLT_CLOSE = register("sniper_bolt_close");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, name);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
    }

    // Вызывается из MagicWandMod.onInitialize(), чтобы звуки зарегистрировались вовремя
    public static void initialize() {
    }
}
