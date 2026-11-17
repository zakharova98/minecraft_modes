package com.example.magicwand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TeleportData(double x, double y, double z, float yaw, float pitch, String dimension) {

    // Кодек для сохранения на диск (в NBT)
    public static final Codec<TeleportData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(TeleportData::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(TeleportData::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(TeleportData::z),
                    Codec.FLOAT.fieldOf("yaw").forGetter(TeleportData::yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(TeleportData::pitch),
                    Codec.STRING.fieldOf("dimension").forGetter(TeleportData::dimension)
            ).apply(instance, TeleportData::new)
    );

    // Кодек для передачи по сети (чтобы работало в мультиплеере)
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, TeleportData::x,
            ByteBufCodecs.DOUBLE, TeleportData::y,
            ByteBufCodecs.DOUBLE, TeleportData::z,
            ByteBufCodecs.FLOAT, TeleportData::yaw,
            ByteBufCodecs.FLOAT, TeleportData::pitch,
            ByteBufCodecs.STRING_UTF8, TeleportData::dimension,
            TeleportData::new
    );
}