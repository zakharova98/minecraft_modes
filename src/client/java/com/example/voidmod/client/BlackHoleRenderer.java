package com.example.voidmod.client;

import com.example.voidmod.entity.BlackHoleEntity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity, EntityRenderState> {

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}