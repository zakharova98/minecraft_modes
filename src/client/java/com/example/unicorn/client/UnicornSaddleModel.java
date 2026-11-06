package com.example.unicorn.client;

import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;

public class UnicornSaddleModel extends EquineSaddleModel {

    public UnicornSaddleModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(EquineRenderState state) {
        super.setupAnim(state);

        ModelPart headParts = root().getChild("head_parts");

        // Убираем детали уздечки около рога
        headParts.getChild("head_saddle").visible = false;

        headParts.getChild("mouth_saddle_wrap").visible = false;

        headParts.getChild("left_saddle_mouth").visible = false;

        headParts.getChild("right_saddle_mouth").visible = false;

        headParts.getChild("left_saddle_line").visible = false;

        headParts.getChild("right_saddle_line").visible = false;
    }
}