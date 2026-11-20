
package com.example.golem.client;

import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;

public class StorageGolemRenderer extends IronGolemRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "golem",
                    "textures/entity/storage_golem.png"
            );

    public StorageGolemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        return TEXTURE;
    }
}
