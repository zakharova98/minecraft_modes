package com.example.unicorn.network;

import com.example.magicwand.MagicWandMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UnicornSprintPayload(boolean active)
        implements CustomPacketPayload {

    public static final Type<UnicornSprintPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            MagicWandMod.MOD_ID,
                            "unicorn_sprint"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, UnicornSprintPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            UnicornSprintPayload::active,
            UnicornSprintPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}