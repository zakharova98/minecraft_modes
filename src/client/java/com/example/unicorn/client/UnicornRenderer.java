package com.example.unicorn.client;

import com.example.magicwand.MagicWandMod;
import com.example.unicorn.UnicornEntity;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.model.geom.ModelLayers;

public class UnicornRenderer extends AbstractHorseRenderer<
        UnicornEntity,
        EquineRenderState,
        AbstractEquineModel<EquineRenderState>
        > {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "unicorn",
                    "textures/entity/unicorn.png"
            );

    public UnicornRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HorseModel(context.bakeLayer(ModelLayers.HORSE)),
                new BabyHorseModel(context.bakeLayer(ModelLayers.HORSE_BABY))
        );
        this.addLayer(
                new SimpleEquipmentLayer<>(
                        this,
                        context.getEquipmentRenderer(),
                        EquipmentClientInfo.LayerType.HORSE_SADDLE,
                        state -> state.saddle,
                        new EquineSaddleModel(
                                context.bakeLayer(ModelLayers.HORSE_SADDLE)
                        ),
                        null
                )
        );

    }

    @Override
    public EquineRenderState createRenderState() {
        return new EquineRenderState();
    }

    @Override
    public Identifier getTextureLocation(EquineRenderState state) {
        return TEXTURE;
    }
}