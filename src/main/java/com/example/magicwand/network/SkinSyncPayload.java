package com.example.magicwand.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Сервер -> клиент: "у игрока uuid такой скин и такие руки".
 * slim = true  -> тонкие руки (Алекс), false -> обычные (Стив).
 * Пустой png = у игрока нет кастомного скина, рисовать стандартный.
 */
public record SkinSyncPayload(UUID playerUuid, boolean slim, byte[] png) implements CustomPacketPayload {

    public static final Type<SkinSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("magicwand", "skin_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinSyncPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SkinSyncPayload::playerUuid,
            ByteBufCodecs.BOOL, SkinSyncPayload::slim,
            ByteBufCodecs.BYTE_ARRAY, SkinSyncPayload::png,
            SkinSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}
