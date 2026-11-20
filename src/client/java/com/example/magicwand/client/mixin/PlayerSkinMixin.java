package com.example.magicwand.client.mixin;

import com.example.magicwand.client.SkinManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class PlayerSkinMixin {

    /**
     * Если для игрока есть кастомный скин (с нужным типом рук Стив/Алекс) - отдаём его вместо стандартного.
     * Тип рук лежит прямо в PlayerSkin, поэтому игра сама выбирает нужную модель, в том числе в первом лице.
     */
    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void magicwand$overrideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        PlayerSkin custom = SkinManager.getPlayerSkin(player.getUUID());
        if (custom != null) {
            cir.setReturnValue(custom);
        }
    }
}
