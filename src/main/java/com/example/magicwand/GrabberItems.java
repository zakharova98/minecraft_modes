package com.example.magicwand;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Регистрация предмета «Захват» и двух компонентов, в которых он хранит,
 * что именно сейчас несёт игрок.
 */
public class GrabberItems {

    public static final ResourceKey<Item> GRABBER_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "grabber")
            );

    public static final Item GRABBER = Registry.register(
            BuiltInRegistries.ITEM,
            GRABBER_KEY,
            new GrabberItem(
                    new Item.Properties()
                            .setId(GRABBER_KEY)
                            .stacksTo(1)
            )
    );

    // Несомый блок (вместе со всеми его свойствами: направление, ось и т.д.)
    public static final DataComponentType<BlockState> CARRIED_BLOCK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "carried_block"),
            DataComponentType.<BlockState>builder()
                    .persistent(BlockState.CODEC)
                    .networkSynchronized(ByteBufCodecs.idMapper(
                            Block.BLOCK_STATE_REGISTRY::byId,
                            Block.BLOCK_STATE_REGISTRY::getId
                    ))
                    .build()
    );

    // Метка «несу существо» (название его вида). Само существо хранится в Grabber.ENTITIES
    public static final DataComponentType<String> CARRIED_ENTITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(MagicWandMod.MOD_ID, "carried_entity"),
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    public static void initialize() {
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(tab -> tab.accept(GRABBER));
    }
}