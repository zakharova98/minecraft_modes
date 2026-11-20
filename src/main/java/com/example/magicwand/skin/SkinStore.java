package com.example.magicwand.skin;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Серверное хранилище скинов: config/magicwand/server_skins/<uuid>.png + <uuid>.properties.
 * Благодаря этому скины переживают перезапуск сервера и приходят тем, кто зашёл позже.
 */
public final class SkinStore {

    /** AUTO - руки определяются по самому файлу скина, SLIM/WIDE - выбор игрока командой /skin model. */
    public enum ModelPref { AUTO, SLIM, WIDE }

    public record Entry(byte[] png, boolean detectedSlim, ModelPref pref) {
        public boolean hasSkin() {
            return png.length > 0;
        }

        public boolean slim() {
            return switch (pref) {
                case SLIM -> true;
                case WIDE -> false;
                case AUTO -> detectedSlim;
            };
        }

        public Entry withPng(byte[] newPng, boolean newDetectedSlim) {
            return new Entry(newPng, newDetectedSlim, pref);
        }

        public Entry withPref(ModelPref newPref) {
            return new Entry(png, detectedSlim, newPref);
        }
    }

    private static final Map<UUID, Entry> CACHE = new ConcurrentHashMap<>();
    private static Path dir;

    private SkinStore() {}

    public static void init() {
        dir = FabricLoader.getInstance().getConfigDir().resolve("magicwand").resolve("server_skins");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось создать " + dir + ": " + e);
        }
    }

    public static Entry get(UUID id) {
        Entry cached = CACHE.get(id);
        if (cached != null) return cached;
        Entry loaded = load(id);
        if (loaded != null) CACHE.put(id, loaded);
        return loaded;
    }

    public static void put(UUID id, Entry entry) {
        CACHE.put(id, entry);
        try {
            Files.createDirectories(dir);
            if (entry.hasSkin()) {
                Files.write(pngPath(id), entry.png());
            } else {
                Files.deleteIfExists(pngPath(id));
            }
            Properties props = new Properties();
            props.setProperty("model", entry.pref().name().toLowerCase(Locale.ROOT));
            props.setProperty("detected_slim", Boolean.toString(entry.detectedSlim()));
            try (OutputStream out = Files.newOutputStream(propsPath(id))) {
                props.store(out, "magicwand skin");
            }
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось сохранить скин " + id + ": " + e);
        }
    }

    public static void remove(UUID id) {
        CACHE.remove(id);
        try {
            Files.deleteIfExists(pngPath(id));
            Files.deleteIfExists(propsPath(id));
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось удалить скин " + id + ": " + e);
        }
    }

    private static Entry load(UUID id) {
        Path png = pngPath(id);
        Path props = propsPath(id);
        if (!Files.exists(png) && !Files.exists(props)) return null;
        try {
            byte[] bytes = Files.exists(png) ? Files.readAllBytes(png) : new byte[0];
            ModelPref pref = ModelPref.AUTO;
            boolean detected = false;
            if (Files.exists(props)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(props)) {
                    p.load(in);
                }
                try {
                    pref = ModelPref.valueOf(p.getProperty("model", "auto").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    // оставляем AUTO
                }
                detected = Boolean.parseBoolean(p.getProperty("detected_slim", "false"));
            }
            return new Entry(bytes, detected, pref);
        } catch (IOException e) {
            System.err.println("[magicwand] Не удалось прочитать скин " + id + ": " + e);
            return null;
        }
    }

    private static Path pngPath(UUID id) {
        return dir.resolve(id + ".png");
    }

    private static Path propsPath(UUID id) {
        return dir.resolve(id + ".properties");
    }
}
