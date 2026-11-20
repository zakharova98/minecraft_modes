package com.example.magicwand.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Клиент -> сервер: "вот мой скин".
 * UUID сюда НЕ кладём: сервер берёт его у отправителя, поэтому подделать чужой скин нельзя.
 * Пустой png = "убрать мой кастомный скин".
 * Лимит серверного пакета в Minecraft - 32 767 байт, нормализованный скин 64x64 весит в разы меньше.
 */
public record SkinUploadPayload(boolean detectedSlim, byte[] png) implements CustomPacketPayload {

    public static final Type<SkinUploadPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("magicwand", "skin_upload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinUploadPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SkinUploadPayload::detectedSlim,
            ByteBufCodecs.BYTE_ARRAY, SkinUploadPayload::png,
            SkinUploadPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
