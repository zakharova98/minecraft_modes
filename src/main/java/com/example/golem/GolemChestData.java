
package com.example.golem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GolemChestData extends SavedData {

    private record ChestEntry(
            String dimension,
            int x,
            int y,
            int z,
            String category
    ) {
        static final Codec<ChestEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.STRING.fieldOf("dimension")
                                .forGetter(ChestEntry::dimension),
                        Codec.INT.fieldOf("x")
                                .forGetter(ChestEntry::x),
                        Codec.INT.fieldOf("y")
                                .forGetter(ChestEntry::y),
                        Codec.INT.fieldOf("z")
                                .forGetter(ChestEntry::z),
                        Codec.STRING.fieldOf("category")
                                .forGetter(ChestEntry::category)
                ).apply(instance, ChestEntry::new));
    }

    private final Map<String, GolemCategory> chests =
            new HashMap<>();

    private static final Codec<GolemChestData> CODEC =
            ChestEntry.CODEC.listOf().xmap(
                    GolemChestData::new,
                    GolemChestData::toEntries
            );

    private static final SavedDataType<GolemChestData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            "magicwand",
                            "golem_chests"
                    ),
                    GolemChestData::new,
                    CODEC,
                    null
            );

    public GolemChestData() {
    }

    private GolemChestData(List<ChestEntry> entries) {
        for (ChestEntry entry : entries) {
            try {
                GolemCategory category =
                        GolemCategory.valueOf(entry.category());

                chests.put(
                        makeKey(
                                entry.dimension(),
                                new BlockPos(entry.x(), entry.y(), entry.z())
                        ),
                        category
                );
            } catch (IllegalArgumentException ignored) {
                // Пропускаем неизвестную категорию.
            }
        }
    }

    private List<ChestEntry> toEntries() {
        return chests.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|");

                    return new ChestEntry(
                            parts[0],
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            entry.getValue().name()
                    );
                })
                .toList();
    }

    public static GolemChestData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    private static String makeKey(String dimension, BlockPos pos) {
        return dimension + "|" +
                pos.getX() + "|" +
                pos.getY() + "|" +
                pos.getZ();
    }

    public void assign(
            ServerLevel level,
            BlockPos pos,
            GolemCategory category
    ) {
        chests.put(
                makeKey(level.dimension().identifier().toString(), pos),
                category
        );

        setDirty();
    }

    public GolemCategory getCategory(
            ServerLevel level,
            BlockPos pos
    ) {
        return chests.get(
                makeKey(level.dimension().identifier().toString(), pos)
        );
    }

    public void remove(ServerLevel level, BlockPos pos) {
        if (chests.remove(
                makeKey(level.dimension().identifier().toString(), pos)
        ) != null) {
            setDirty();
        }
    }
}
