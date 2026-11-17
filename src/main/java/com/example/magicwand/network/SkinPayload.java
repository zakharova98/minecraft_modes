package com.example.magicwand.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.UUIDUtil;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import java.util.UUID;

public record SkinPayload(UUID playerUuid, byte[] skinBytes) implements CustomPacketPayload {

    public static final Type<SkinPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("magicwand", "skin_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SkinPayload::playerUuid,
            ByteBufCodecs.BYTE_ARRAY, SkinPayload::skinBytes,
            SkinPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        // В 26.x playC2S/playS2C переименованы в serverboundPlay/clientboundPlay [citation:5][citation:10]
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            if (payload.skinBytes().length > 1024 * 1024) return; // Лимит 1 МБ

            ServerPlayer sender = context.player();

            // В 26.3 у ServerPlayer нет getServer(), получаем сервер через level()
            MinecraftServer server = sender.level().getServer();
            if (server == null) return;

            // PlayerLookup.all(server) возвращает всех игроков на сервере [citation:3]
            for (ServerPlayer player : PlayerLookup.all(server)) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }
}