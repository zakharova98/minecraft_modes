package com.example.magicwand.skin;

import com.example.magicwand.network.SkinSyncPayload;
import com.example.magicwand.network.SkinUploadPayload;
import com.example.magicwand.skin.SkinStore.Entry;
import com.example.magicwand.skin.SkinStore.ModelPref;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Серверная часть: принимает скины, хранит их и раздаёт всем (в том числе тем, кто зашёл позже). */
public final class SkinServer {

    /** Размер PNG-пакета от клиента не может превышать 32 767 байт (лимит Minecraft). */
    private static final int MAX_PNG_BYTES = 30_000;
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    private static final Set<UUID> KNOWN_ONLINE = new HashSet<>();

    private SkinServer() {}

    public static void init() {
        SkinStore.init();

        ServerPlayNetworking.registerGlobalReceiver(SkinUploadPayload.TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();
            MinecraftServer server = sender.level().getServer();
            if (server == null) return;
            // UUID берём у отправителя, а не из пакета - так нельзя подменить чужой скин
            handleUpload(server, sender.getUUID(), payload.png(), payload.detectedSlim());
        });

        ServerTickEvents.END_SERVER_TICK.register(SkinServer::tick);
    }

    // ---------- вход игроков ----------

    private static void tick(MinecraftServer server) {
        Collection<ServerPlayer> players = PlayerLookup.all(server);
        Set<UUID> now = new HashSet<>();
        for (ServerPlayer player : players) {
            now.add(player.getUUID());
            if (KNOWN_ONLINE.add(player.getUUID())) {
                onJoin(server, player, players);
            }
        }
        KNOWN_ONLINE.retainAll(now);
    }

    private static void onJoin(MinecraftServer server, ServerPlayer joined, Collection<ServerPlayer> everyone) {
        // 1) новичку - скины всех, кто уже на сервере
        for (ServerPlayer other : everyone) {
            if (other == joined) continue;
            Entry e = SkinStore.get(other.getUUID());
            if (e != null && e.hasSkin()) {
                send(joined, new SkinSyncPayload(other.getUUID(), e.slim(), e.png()));
            }
        }
        // 2) всем (и ему самому) - сохранённый скин новичка
        Entry mine = SkinStore.get(joined.getUUID());
        if (mine != null && mine.hasSkin()) {
            broadcast(server, joined.getUUID(), mine);
        }
    }

    // ---------- загрузка и настройки ----------

    private static void handleUpload(MinecraftServer server, UUID id, byte[] png, boolean detectedSlim) {
        Entry old = SkinStore.get(id);

        if (png.length == 0) {
            reset(server, id);
            return;
        }
        if (!isValidSkinPng(png)) return;

        if (old != null && old.hasSkin() && old.detectedSlim() == detectedSlim && Arrays.equals(old.png(), png)) {
            return; // тот же скин, ничего не делаем
        }
        Entry updated = (old == null)
                ? new Entry(png, detectedSlim, ModelPref.AUTO)
                : old.withPng(png, detectedSlim);
        SkinStore.put(id, updated);
        broadcast(server, id, updated);
    }

    public static void setPref(MinecraftServer server, UUID id, ModelPref pref) {
        Entry old = SkinStore.get(id);
        Entry updated = (old == null) ? new Entry(new byte[0], false, pref) : old.withPref(pref);
        SkinStore.put(id, updated);
        if (updated.hasSkin()) {
            broadcast(server, id, updated);
        }
    }

    public static void reset(MinecraftServer server, UUID id) {
        SkinStore.remove(id);
        broadcast(server, id, new Entry(new byte[0], false, ModelPref.AUTO));
    }

    // ---------- рассылка ----------

    private static void broadcast(MinecraftServer server, UUID id, Entry entry) {
        SkinSyncPayload payload = new SkinSyncPayload(id, entry.slim(), entry.png());
        for (ServerPlayer player : PlayerLookup.all(server)) {
            send(player, payload);
        }
    }

    private static void send(ServerPlayer player, SkinSyncPayload payload) {
        try {
            ServerPlayNetworking.send(player, payload);
        } catch (Exception e) {
            System.err.println("[magicwand] Не удалось отправить скин игроку " + player.getUUID() + ": " + e);
        }
    }

    /** Проверяем PNG без декодирования: подпись + заголовок IHDR + размер ровно 64x64. */
    private static boolean isValidSkinPng(byte[] b) {
        if (b.length < 33 || b.length > MAX_PNG_BYTES) return false;
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (b[i] != PNG_SIGNATURE[i]) return false;
        }
        if (b[12] != 'I' || b[13] != 'H' || b[14] != 'D' || b[15] != 'R') return false;
        int width = readInt(b, 16);
        int height = readInt(b, 20);
        return width == 64 && height == 64;
    }

    private static int readInt(byte[] b, int off) {
        return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16) | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
    }
}
