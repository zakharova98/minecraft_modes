package com.example.unicorn.client;

import com.example.magicwand.MagicWandMod;

import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class UnicornBabyModel extends BabyHorseModel {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath(
                            MagicWandMod.MOD_ID,
                            "unicorn_baby"
                    ),
                    "main"
            );

    public UnicornBabyModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {

        MeshDefinition mesh =
                BabyHorseModel.createBabyMesh(
                        CubeDeformation.NONE
                );

        PartDefinition head = mesh.getRoot()
                .getChild("head_parts")
                .getChild("head");

        // Рог для маленького единорога
        UnicornModel.addHorn(
                head,
                -3.8F,
                -4.7F
        );

        return LayerDefinition.create(mesh, 64, 64);
    }
}