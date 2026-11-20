package com.example.magicwand.dynlight;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Динамический свет (как в OptiFine): игрок с факелом/фонарём/лавой и т.п. в руке освещает местность вокруг себя.
 *
 * Как работает: сервер ставит невидимый блок minecraft:light в воздухе у головы игрока и двигает его вслед за ним.
 * Плюсы: работает на сервере, поэтому свет видят ВСЕ игроки (в том числе чужие факелы), никаких сторонних модов не нужно.
 * Защита от "мусора": при выходе игрока, смене предмета и остановке сервера блоки убираются, а их координаты
 * пишутся в журнал - если игра упадёт, при следующем запуске оставшиеся блоки света будут удалены.
 */
public final class DynamicLights {

    /** 2 = обновить блоки, 16 = не пересчитывать соседей. Тихая замена без каскада обновлений (наблюдатели и т.п.). */
    private static final int FLAGS = 2 | 16;
    private static final int MAX_PENDING = 256;

    private static final class Placed {
        final ServerLevel level;
        final BlockPos pos;
        final BlockState original; // воздух или источник воды - что было до света

        Placed(ServerLevel level, BlockPos pos, BlockState original) {
            this.level = level;
            this.pos = pos;
            this.original = original;
        }
    }

    private static final Map<UUID, Placed> ACTIVE = new HashMap<>();
    /** Блоки, которые не удалось убрать сразу (чанк выгружен) - пробуем ещё раз позже. */
    private static final List<Placed> PENDING = new ArrayList<>();

    private static boolean enabled = true;
    private static int interval = 2;
    private static Map<String, Integer> levels = new LinkedHashMap<>();

    private static boolean dirty = false;
    private static int ticks = 0;

    private DynamicLights() {}

    // ================== инициализация ==================

    public static void init() {
        load();

        ServerLifecycleEvents.SERVER_STARTED.register(DynamicLights::cleanupJournal);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> removeAll());
        ServerTickEvents.END_SERVER_TICK.register(DynamicLights::tick);
    }

    // ================== настройки ==================

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("magicwand").resolve("dynamic_light.txt");
    }

    private static Path journalPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("magicwand").resolve("dynamic_light_journal.txt");
    }

    private static Map<String, Integer> defaultLevels() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("minecraft:torch", 14);
        m.put("minecraft:soul_torch", 10);
        m.put("minecraft:redstone_torch", 7);
        m.put("minecraft:copper_torch", 14);
        m.put("minecraft:lantern", 15);
        m.put("minecraft:soul_lantern", 10);
        m.put("minecraft:copper_lantern", 15);
        m.put("minecraft:glowstone", 15);
        m.put("minecraft:sea_lantern", 15);
        m.put("minecraft:shroomlight", 15);
        m.put("minecraft:jack_o_lantern", 15);
        m.put("minecraft:lava_bucket", 15);
        m.put("minecraft:campfire", 15);
        m.put("minecraft:soul_campfire", 10);
        m.put("minecraft:end_rod", 14);
        m.put("minecraft:beacon", 15);
        m.put("minecraft:conduit", 15);
        m.put("minecraft:ochre_froglight", 15);
        m.put("minecraft:verdant_froglight", 15);
        m.put("minecraft:pearlescent_froglight", 15);
        m.put("minecraft:crying_obsidian", 10);
        m.put("minecraft:magma_block", 3);
        m.put("minecraft:sea_pickle", 6);
        m.put("minecraft:candle", 3);
        m.put("minecraft:light", 15);
        m.put("magicwand:flashlight_on", 15);
        return m;
    }

    /** Читает config/magicwand/dynamic_light.txt, а если его нет - создаёт со значениями по умолчанию. */
    public static void load() {
        Path file = configPath();
        Map<String, Integer> loaded = new LinkedHashMap<>();
        boolean en = true;
        int iv = 2;

        if (Files.exists(file)) {
            try {
                for (String raw : Files.readAllLines(file)) {
                    String line = raw.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq < 0) continue;
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    try {
                        if (key.equals("enabled")) {
                            en = Boolean.parseBoolean(value);
                        } else if (key.equals("update_interval_ticks")) {
                            iv = Math.max(1, Math.min(20, Integer.parseInt(value)));
                        } else if (key.contains(":")) {
                            loaded.put(key, Math.max(0, Math.min(15, Integer.parseInt(value))));
                        }
                    } catch (NumberFormatException ignored) {
                        System.err.println("[magicwand] dynamic_light.txt: непонятное значение в строке \"" + line + "\"");
                    }
                }
            } catch (IOException e) {
                System.err.println("[magicwand] Не удалось прочитать " + file + ": " + e);
                loaded = defaultLevels();
            }
        } else {
            loaded = defaultLevels();
        }

        enabled = en;
        interval = iv;
        levels = loaded;
        if (!Files.exists(file)) {
            save();
        }
    }

    public static void save() {
        List<String> lines = new ArrayList<>();
        lines.add("# Динамический свет (MagicMod).");
        lines.add("# enabled - включён ли динамический свет на сервере. Меняется командой /dynlight on|off.");
        lines.add("# update_interval_ticks - как часто двигать свет (1-20 тиков; меньше = плавнее, но чуть тяжелее).");
        lines.add("# Ниже: предмет = уровень света (0-15). Свет даёт предмет в любой руке (берётся самый яркий).");
        lines.add("# После правки файла примените его командой /dynlight reload.");
        lines.add("enabled = " + enabled);
        lines.add("update_interval_ticks = " + interval);
        lines.add("");
        for (Map.Entry<String, Integer> e : levels.entrySet()) {
            lines.add(e.getKey() + " = " + e.getValue());
        }
        try {
            Files.createDirectories(configPath().getParent());
            Files.write(configPath(), lines);
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось сохранить " + configPath() + ": " + e);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            removeAll();
        }
        save();
    }

    public static int itemCount() {
        return levels.size();
    }

    // ================== основной цикл ==================

    private static void tick(MinecraftServer server) {
        if (!enabled) {
            if (!ACTIVE.isEmpty() || !PENDING.isEmpty()) removeAll();
            return;
        }
        ticks++;
        if (ticks % interval != 0) return;

        retryPending();

        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : PlayerLookup.all(server)) {
            online.add(player.getUUID());
            update(player);
        }

        // игроки, которые вышли, - убираем их свет
        Iterator<Map.Entry<UUID, Placed>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Placed> e = it.next();
            if (!online.contains(e.getKey())) {
                release(e.getValue());
                it.remove();
            }
        }

        if (dirty && ticks % 20 == 0) {
            writeJournal();
        }
    }

    private static void update(ServerPlayer player) {
        UUID id = player.getUUID();
        Placed current = ACTIVE.get(id);

        int level = (player.isSpectator() || !player.isAlive()) ? 0 : lightLevel(player);
        if (level <= 0 || !(player.level() instanceof ServerLevel sl)) {
            if (current != null) {
                release(current);
                ACTIVE.remove(id);
            }
            return;
        }

        // сменил измерение - старый свет убираем
        if (current != null && current.level != sl) {
            release(current);
            ACTIVE.remove(id);
            current = null;
        }
        // кто-то заменил наш блок света (например, поставил блок на это место)
        if (current != null && !current.level.getBlockState(current.pos).is(Blocks.LIGHT)) {
            ACTIVE.remove(id);
            dirty = true;
            current = null;
        }

        // куда ставить: у глаз, иначе у ног, иначе над головой
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        BlockPos[] candidates = {eye, player.blockPosition(), eye.above()};
        BlockPos target = null;
        for (BlockPos c : candidates) {
            if ((current != null && current.pos.equals(c)) || canHost(sl, c)) {
                target = c;
                break;
            }
        }
        if (target == null) {
            if (current != null) {
                release(current);
                ACTIVE.remove(id);
            }
            return;
        }

        if (current != null && current.pos.equals(target)) {
            // остались на месте - при необходимости просто меняем яркость
            BlockState s = sl.getBlockState(target);
            if (s.is(Blocks.LIGHT) && s.getValue(BlockStateProperties.LEVEL) != level) {
                sl.setBlock(target, s.setValue(BlockStateProperties.LEVEL, level), FLAGS);
            }
            return;
        }

        // переехали: сначала ставим новый, потом убираем старый - чтобы свет не мигал
        BlockState original = sl.getBlockState(target);
        BlockState light = Blocks.LIGHT.defaultBlockState()
                .setValue(BlockStateProperties.LEVEL, level)
                .setValue(BlockStateProperties.WATERLOGGED, original.is(Blocks.WATER));
        sl.setBlock(target, light, FLAGS);
        ACTIVE.put(id, new Placed(sl, target.immutable(), original));
        dirty = true;
        if (current != null) {
            release(current);
        }
    }

    private static boolean canHost(ServerLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || (s.is(Blocks.WATER) && s.getFluidState().isSource());
    }

    private static int lightLevel(ServerPlayer player) {
        return Math.max(levelOf(player.getMainHandItem()), levelOf(player.getOffhandItem()));
    }

    private static int levelOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        String key = BuiltInRegistries.ITEM.getKey(item).toString();
        return levels.getOrDefault(key, 0);
    }

    // ================== удаление света ==================

    /** Убирает блок. false - если чанк сейчас не загружен (нельзя трогать, чтобы не принудительно грузить). */
    private static boolean removeNow(Placed p) {
        if (!p.level.hasChunkAt(p.pos)) return false;
        BlockState cur = p.level.getBlockState(p.pos);
        if (cur.is(Blocks.LIGHT)) {
            p.level.setBlock(p.pos, p.original, FLAGS);
        }
        return true;
    }

    private static void release(Placed p) {
        if (!removeNow(p) && PENDING.size() < MAX_PENDING) {
            PENDING.add(p);
        }
        dirty = true;
    }

    private static void retryPending() {
        if (PENDING.isEmpty()) return;
        Iterator<Placed> it = PENDING.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            if (removeNow(it.next())) {
                it.remove();
                changed = true;
            }
        }
        if (changed) dirty = true;
    }

    private static void removeAll() {
        for (Placed p : ACTIVE.values()) {
            if (!removeNow(p) && PENDING.size() < MAX_PENDING) PENDING.add(p);
        }
        ACTIVE.clear();
        retryPending();
        writeJournal();
    }

    // ================== журнал (защита от крашей) ==================

    private static String dimKey(ServerLevel level) {
        return level.dimension().toString();
    }

    private static void writeJournal() {
        dirty = false;
        Path file = journalPath();
        try {
            List<Placed> all = new ArrayList<>(ACTIVE.values());
            all.addAll(PENDING);
            if (all.isEmpty()) {
                Files.deleteIfExists(file);
                return;
            }
            List<String> lines = new ArrayList<>();
            for (Placed p : all) {
                lines.add(dimKey(p.level) + "\t" + p.pos.getX() + "\t" + p.pos.getY() + "\t" + p.pos.getZ()
                        + "\t" + (p.original.is(Blocks.WATER) ? "w" : "a"));
            }
            Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось записать журнал динамического света: " + e);
        }
    }

    /** При старте сервера убираем блоки света, оставшиеся после аварийного завершения. */
    private static void cleanupJournal(MinecraftServer server) {
        Path file = journalPath();
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split("\t");
                if (parts.length < 5) continue;
                try {
                    BlockPos pos = new BlockPos(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    for (ServerLevel level : server.getAllLevels()) {
                        if (!dimKey(level).equals(parts[0])) continue;
                        if (level.getBlockState(pos).is(Blocks.LIGHT)) {
                            BlockState restore = parts[4].equals("w") ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                            level.setBlock(pos, restore, FLAGS);
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // битая строка - пропускаем
                }
            }
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось прочитать журнал динамического света: " + e);
        }
    }
}
