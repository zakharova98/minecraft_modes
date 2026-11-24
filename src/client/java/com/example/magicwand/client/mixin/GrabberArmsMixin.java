package com.example.magicwand.client.mixin;

import com.example.magicwand.Grabber;
import com.example.magicwand.GrabberItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Поза обеих рук: удерживание груза перед грудью. */
@Mixin(HumanoidModel.class)
public abstract class GrabberArmsMixin {
    // Руки объявлены в HumanoidModel, а не непосредственно в PlayerModel.
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void magicwand$carryPose(HumanoidRenderState state, CallbackInfo ci) {
        // HumanoidModel используется и другими мобами; изменяем позу только для игрока.
        if (!(state instanceof AvatarRenderState)) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || !player.getMainHandItem().is(GrabberItems.GRABBER)
                || !Grabber.isCarrying(player.getMainHandItem())) return;

        // Обе руки согнуты вперёд и немного сведены к центру.
        rightArm.xRot = -1.18F;
        rightArm.yRot = -0.28F;
        rightArm.zRot = 0.12F;
        leftArm.xRot = -1.18F;
        leftArm.yRot = 0.28F;
        leftArm.zRot = -0.12F;
    }
}
