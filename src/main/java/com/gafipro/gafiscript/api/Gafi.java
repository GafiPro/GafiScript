package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.scheduler.GafiScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class Gafi {
    private static volatile MinecraftServer server;
    private static final GafiScheduler SCHEDULER = new GafiScheduler();

    private Gafi() {}

    public static void attachServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
        SCHEDULER.attach(minecraftServer);
    }

    public static void detachServer(MinecraftServer minecraftServer) {
        if (server == minecraftServer) {
            SCHEDULER.detach();
            server = null;
        }
    }

    public static MinecraftServer server() {
        MinecraftServer current = server;
        if (current == null) {
            throw new IllegalStateException("GafiScript is not attached to a running server.");
        }
        return current;
    }

    public static GafiScheduler scheduler() {
        return SCHEDULER;
    }

    public static GafiEvents events() {
        return GafiEvents.INSTANCE;
    }

    public static GafiWorld world() {
        return new GafiWorld(server());
    }

    public static List<GafiPlayer> players() {
        return server().getPlayerManager().getPlayerList()
                .stream()
                .map(GafiPlayer::new)
                .toList();
    }

    public static GafiPlayer player(String name) {
        ServerPlayerEntity player = server().getPlayerManager().getPlayer(name);
        return player == null ? null : new GafiPlayer(player);
    }

    public static GafiPlayer playerByUuid(String uuid) {
        try {
            ServerPlayerEntity player = server().getPlayerManager().getPlayer(java.util.UUID.fromString(uuid));
            return player == null ? null : new GafiPlayer(player);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static GafiTask delayTicks(long ticks, Runnable action) {
        return scheduler().delayTicks(ticks, action);
    }

    public static GafiTask delaySeconds(double seconds, Runnable action) {
        return scheduler().delaySeconds(seconds, action);
    }

    public static GafiTask repeatTicks(long ticks, Runnable action) {
        return scheduler().repeatTicks(ticks, action);
    }

    public static GafiTask repeatSeconds(double seconds, Runnable action) {
        return scheduler().repeatSeconds(seconds, action);
    }

    public static void broadcast(String message) {
        Text text = Text.literal(String.valueOf(message));
        server().execute(() -> server().getPlayerManager().broadcast(text, false));
    }

    public static void logInfo(String message) {
        com.gafipro.gafiscript.GafiScriptMod.LOGGER.info("[Script] {}", message);
    }

    public static void logWarn(String message) {
        com.gafipro.gafiscript.GafiScriptMod.LOGGER.warn("[Script] {}", message);
    }

    public static void logError(String message) {
        com.gafipro.gafiscript.GafiScriptMod.LOGGER.error("[Script] {}", message);
    }

    public static void runSync(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        server().execute(runnable);
    }
}
