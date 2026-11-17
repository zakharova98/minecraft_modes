package com.example.magicwand.client.mixin;

import com.example.magicwand.client.SkinManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.core.ClientAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class PlayerSkinMixin {

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void overrideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

        // Пытаемся получить кастомный скин из нашего менеджера
        Identifier customSkin = SkinManager.getSkin(player.getUUID());

        if (customSkin != null) {
            ClientAsset.Texture bodyTexture = new ClientAsset.Texture() {
                @Override public Identifier id() { return customSkin; }
                @Override public Identifier texturePath() { return customSkin; }
            };

            cir.setReturnValue(new PlayerSkin(
                    bodyTexture, null, null, PlayerModelType.WIDE, false
            ));
        }
    }
}