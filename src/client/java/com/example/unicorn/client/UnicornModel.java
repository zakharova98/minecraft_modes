package com.example.unicorn.client;

import com.example.magicwand.MagicWandMod;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class UnicornModel extends HorseModel {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath(
                            MagicWandMod.MOD_ID,
                            "unicorn"
                    ),
                    "main"
            );

    public UnicornModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {

        MeshDefinition mesh =
                AbstractEquineModel.createBodyMesh(
                        CubeDeformation.NONE
                );

        // Находим голову лошади
        PartDefinition head = mesh.getRoot()
                .getChild("head_parts")
                .getChild("head");

        // Добавляем рог
        addHorn(head, -9.0F, -1.4F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    static void addHorn(
            PartDefinition head,
            float offsetY,
            float offsetZ
    ) {

        head.addOrReplaceChild(
                "unicorn_horn",

                CubeListBuilder.create()

                        // Основание рога
                        .texOffs(48, 0)
                        .addBox(
                                -1.0F, -2.0F, -1.0F,
                                2.0F, 2.0F, 2.0F
                        )

                        // Второй сегмент
                        .texOffs(48, 5)
                        .addBox(
                                -0.75F, -4.0F, -0.75F,
                                1.5F, 2.0F, 1.5F
                        )

                        // Третий сегмент
                        .texOffs(48, 10)
                        .addBox(
                                -0.5F, -6.0F, -0.5F,
                                1.0F, 2.0F, 1.0F
                        )

                        // Острый кончик
                        .texOffs(48, 15)
                        .addBox(
                                -0.25F, -7.0F, -0.25F,
                                0.5F, 1.0F, 0.5F
                        ),

                PartPose.offset(
                        0.0F,
                        offsetY,
                        offsetZ
                )
        );
    }
}