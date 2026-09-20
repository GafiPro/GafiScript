package com.gafipro.gafiscript.api;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class GafiEvents {
    public static final GafiEvents INSTANCE = new GafiEvents();

    private final List<Consumer<GafiPlayer>> joinListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<GafiPlayer>> leaveListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<GafiPlayer>> deathListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<GafiBlockBreakEvent>> breakListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<GafiBlockUseEvent>> useListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<GafiServerTickEvent>> tickListeners = new CopyOnWriteArrayList<>();
    private boolean registered;

    private GafiEvents() {}

    public static void register() {
        if (INSTANCE.registered) return;
        INSTANCE.registered = true;

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                INSTANCE.joinListeners.forEach(listener -> safe(() -> listener.accept(new GafiPlayer(handler.getPlayer())))));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                INSTANCE.leaveListeners.forEach(listener -> safe(() -> listener.accept(new GafiPlayer(handler.getPlayer())))));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                INSTANCE.deathListeners.forEach(listener -> safe(() -> listener.accept(new GafiPlayer(oldPlayer))));
            }
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }

            GafiBlockBreakEvent event = new GafiBlockBreakEvent(
                    new GafiPlayer(serverPlayer),
                    new GafiPosition(pos.getX(), pos.getY(), pos.getZ()),
                    state
            );

            INSTANCE.breakListeners.forEach(listener -> safe(() -> listener.accept(event)));
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            GafiBlockUseEvent event = new GafiBlockUseEvent(
                    new GafiPlayer(serverPlayer),
                    new GafiPosition(
                            hitResult.getBlockPos().getX(),
                            hitResult.getBlockPos().getY(),
                            hitResult.getBlockPos().getZ()
                    )
            );

            INSTANCE.useListeners.forEach(listener -> safe(() -> listener.accept(event)));
            return event.isCancelled() ? ActionResult.FAIL : ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server ->
                INSTANCE.tickListeners.forEach(listener -> safe(() -> listener.accept(new GafiServerTickEvent()))));
    }

    public void onPlayerJoin(Consumer<GafiPlayer> listener) {
        joinListeners.add(listener);
    }

    public void onPlayerLeave(Consumer<GafiPlayer> listener) {
        leaveListeners.add(listener);
    }

    public void onPlayerDeath(Consumer<GafiPlayer> listener) {
        deathListeners.add(listener);
    }

    public void onBlockBreak(Consumer<GafiBlockBreakEvent> listener) {
        breakListeners.add(listener);
    }

    public void onBlockUse(Consumer<GafiBlockUseEvent> listener) {
        useListeners.add(listener);
    }

    public void onTick(Consumer<GafiServerTickEvent> listener) {
        tickListeners.add(listener);
    }

    private static void safe(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            com.gafipro.gafiscript.GafiScriptMod.LOGGER.error("GafiScript event listener failed", throwable);
        }
    }

    public static final class GafiBlockBreakEvent {
        private final GafiPlayer player;
        private final GafiPosition position;
        private final net.minecraft.block.BlockState state;

        public GafiBlockBreakEvent(GafiPlayer player, GafiPosition position, net.minecraft.block.BlockState state) {
            this.player = player;
            this.position = position;
            this.state = state;
        }

        public GafiPlayer player() { return player; }
        public GafiPosition position() { return position; }
        public String blockId() {
            return net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString();
        }
    }

    public static final class GafiBlockUseEvent {
        private final GafiPlayer player;
        private final GafiPosition position;
        private boolean cancelled;

        public GafiBlockUseEvent(GafiPlayer player, GafiPosition position) {
            this.player = player;
            this.position = position;
        }

        public GafiPlayer player() { return player; }
        public GafiPosition position() { return position; }
        public boolean isCancelled() { return cancelled; }
        public void cancel() { cancelled = true; }
    }

    public record GafiServerTickEvent() {}
}
