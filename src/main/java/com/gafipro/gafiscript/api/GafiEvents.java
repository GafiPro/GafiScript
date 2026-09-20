package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.runtime.GafiScriptContext;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class GafiEvents {
    public static final GafiEvents INSTANCE = new GafiEvents();

    private final List<Listener<GafiPlayer>> joinListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiPlayer>> leaveListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiPlayer>> deathListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiBlockBreakEvent>> breakListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiBlockUseEvent>> useListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiItemUseEvent>> itemUseListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiEntityUseEvent>> entityUseListeners =
            new CopyOnWriteArrayList<>();
    private final List<Listener<GafiServerTickEvent>> tickListeners =
            new CopyOnWriteArrayList<>();

    private boolean registered;

    private GafiEvents() {}

    public static void register() {
        if (INSTANCE.registered) return;

        INSTANCE.registered = true;

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        INSTANCE.joinListeners.forEach(
                                listener ->
                                        INSTANCE.safe(
                                                listener.ownerScript(),
                                                () -> listener.consumer()
                                                        .accept(
                                                                new GafiPlayer(
                                                                        handler.getPlayer()
                                                                )
                                                        )
                                        )
                        )
        );

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) ->
                        INSTANCE.leaveListeners.forEach(
                                listener ->
                                        INSTANCE.safe(
                                                listener.ownerScript(),
                                                () -> listener.consumer()
                                                        .accept(
                                                                new GafiPlayer(
                                                                        handler.getPlayer()
                                                                )
                                                        )
                                        )
                        )
        );

        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> {
                    if (!alive) {
                        INSTANCE.deathListeners.forEach(
                                listener ->
                                        INSTANCE.safe(
                                                listener.ownerScript(),
                                                () -> listener.consumer()
                                                        .accept(
                                                                new GafiPlayer(
                                                                        oldPlayer
                                                                )
                                                        )
                                        )
                        );
                    }
                }
        );

        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, entity) -> {
                    if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return;
                    }

                    GafiBlockBreakEvent event =
                            new GafiBlockBreakEvent(
                                    new GafiPlayer(serverPlayer),
                                    new GafiPosition(
                                            pos.getX(),
                                            pos.getY(),
                                            pos.getZ()
                                    ),
                                    state
                            );

                    INSTANCE.breakListeners.forEach(
                            listener ->
                                    INSTANCE.safe(
                                            listener.ownerScript(),
                                            () -> listener.consumer()
                                                    .accept(event)
                                    )
                    );
                }
        );

        UseBlockCallback.EVENT.register(
                (player, world, hand, hitResult) -> {
                    if (world.isClient() ||
                            !(player instanceof ServerPlayerEntity serverPlayer)) {
                        return ActionResult.PASS;
                    }

                    GafiBlockUseEvent event =
                            new GafiBlockUseEvent(
                                    new GafiPlayer(serverPlayer),
                                    new GafiPosition(
                                            hitResult.getBlockPos().getX(),
                                            hitResult.getBlockPos().getY(),
                                            hitResult.getBlockPos().getZ()
                                    )
                            );

                    INSTANCE.useListeners.forEach(
                            listener ->
                                    INSTANCE.safe(
                                            listener.ownerScript(),
                                            () -> listener.consumer()
                                                    .accept(event)
                                    )
                    );

                    return event.isCancelled()
                            ? ActionResult.FAIL
                            : ActionResult.PASS;
                }
        );

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            GafiItemUseEvent event = new GafiItemUseEvent(
                    new GafiPlayer(serverPlayer),
                    serverPlayer.getStackInHand(hand).copy()
            );

            INSTANCE.itemUseListeners.forEach(listener ->
                    INSTANCE.safe(listener.ownerScript(), () -> listener.consumer().accept(event))
            );

            return event.isCancelled() ? ActionResult.FAIL : ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            GafiEntityUseEvent event = new GafiEntityUseEvent(
                    new GafiPlayer(serverPlayer),
                    new GafiEntity(entity)
            );

            INSTANCE.entityUseListeners.forEach(listener ->
                    INSTANCE.safe(listener.ownerScript(), () -> listener.consumer().accept(event))
            );

            return event.isCancelled() ? ActionResult.FAIL : ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(
                server ->
                        INSTANCE.tickListeners.forEach(
                                listener ->
                                        INSTANCE.safe(
                                                listener.ownerScript(),
                                                () -> listener.consumer()
                                                        .accept(
                                                                new GafiServerTickEvent()
                                                        )
                                        )
                        )
        );
    }

    public GafiEventHandle onPlayerJoin(
            Consumer<GafiPlayer> listener
    ) {
        return add(joinListeners, listener);
    }

    public GafiEventHandle onPlayerLeave(
            Consumer<GafiPlayer> listener
    ) {
        return add(leaveListeners, listener);
    }

    public GafiEventHandle onPlayerDeath(
            Consumer<GafiPlayer> listener
    ) {
        return add(deathListeners, listener);
    }

    public GafiEventHandle onBlockBreak(
            Consumer<GafiBlockBreakEvent> listener
    ) {
        return add(breakListeners, listener);
    }

    public GafiEventHandle onBlockUse(
            Consumer<GafiBlockUseEvent> listener
    ) {
        return add(useListeners, listener);
    }

    public GafiEventHandle onItemUse(Consumer<GafiItemUseEvent> listener) {
        return add(itemUseListeners, listener);
    }

    public GafiEventHandle onInteractEntity(Consumer<GafiEntityUseEvent> listener) {
        return add(entityUseListeners, listener);
    }

    public GafiEventHandle onTick(
            Consumer<GafiServerTickEvent> listener
    ) {
        return add(tickListeners, listener);
    }

    public void unregisterOwnedBy(String scriptName) {
        unregisterOwnedBy(joinListeners, scriptName);
        unregisterOwnedBy(leaveListeners, scriptName);
        unregisterOwnedBy(deathListeners, scriptName);
        unregisterOwnedBy(breakListeners, scriptName);
        unregisterOwnedBy(useListeners, scriptName);
        unregisterOwnedBy(itemUseListeners, scriptName);
        unregisterOwnedBy(entityUseListeners, scriptName);
        unregisterOwnedBy(tickListeners, scriptName);
    }

    private <T> GafiEventHandle add(
            List<Listener<T>> listeners,
            Consumer<T> consumer
    ) {
        if (consumer == null) {
            throw new NullPointerException("listener");
        }

        Listener<T> listener =
                new Listener<>(
                        GafiScriptContext.currentScript(),
                        consumer
                );

        GafiEventHandle handle =
                new GafiEventHandle(
                        listener.ownerScript(),
                        () -> listeners.remove(listener)
                );

        listener.attach(handle);
        listeners.add(listener);
        return handle;
    }

    private <T> void unregisterOwnedBy(
            List<Listener<T>> listeners,
            String scriptName
    ) {
        if (scriptName == null) return;

        listeners.removeIf(
                listener -> {
                    if (!scriptName.equals(listener.ownerScript())) {
                        return false;
                    }

                    listener.unregister();
                    return true;
                }
        );
    }

    private void safe(
            String owner,
            Runnable action
    ) {
        try {
            if (owner == null) {
                action.run();
            } else {
                GafiScriptContext.runAs(owner, action);
            }
        } catch (Throwable throwable) {
            com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                    "GafiScript event listener failed",
                    throwable
            );
        }
    }

    private static final class Listener<T> {
        private final String ownerScript;
        private final Consumer<T> consumer;
        private GafiEventHandle handle;

        private Listener(
                String ownerScript,
                Consumer<T> consumer
        ) {
            this.ownerScript = ownerScript;
            this.consumer = consumer;
        }

        String ownerScript() {
            return ownerScript;
        }

        Consumer<T> consumer() {
            return consumer;
        }

        void unregister() {
            if (handle != null) {
                handle.unregister();
            }
        }

        void attach(GafiEventHandle handle) {
            this.handle = handle;
        }
    }

    public static final class GafiBlockBreakEvent {
        private final GafiPlayer player;
        private final GafiPosition position;
        private final net.minecraft.block.BlockState state;

        public GafiBlockBreakEvent(
                GafiPlayer player,
                GafiPosition position,
                net.minecraft.block.BlockState state
        ) {
            this.player = player;
            this.position = position;
            this.state = state;
        }

        public GafiPlayer player() {
            return player;
        }

        public GafiPosition position() {
            return position;
        }

        public String blockId() {
            return net.minecraft.registry.Registries.BLOCK
                    .getId(state.getBlock())
                    .toString();
        }
    }

    public static final class GafiBlockUseEvent {
        private final GafiPlayer player;
        private final GafiPosition position;
        private boolean cancelled;

        public GafiBlockUseEvent(
                GafiPlayer player,
                GafiPosition position
        ) {
            this.player = player;
            this.position = position;
        }

        public GafiPlayer player() {
            return player;
        }

        public GafiPosition position() {
            return position;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            cancelled = true;
        }
    }

    public static final class GafiItemUseEvent {
        private final GafiPlayer player;
        private final net.minecraft.item.ItemStack item;
        private boolean cancelled;

        public GafiItemUseEvent(GafiPlayer player, net.minecraft.item.ItemStack item) {
            this.player = player;
            this.item = item;
        }

        public GafiPlayer player() { return player; }
        public String itemId() {
            return net.minecraft.registry.Registries.ITEM.getId(item.getItem()).toString();
        }
        public net.minecraft.item.ItemStack rawItem() { return item.copy(); }
        public boolean isCancelled() { return cancelled; }
        public void cancel() { cancelled = true; }
    }

    public static final class GafiEntityUseEvent {
        private final GafiPlayer player;
        private final GafiEntity entity;
        private boolean cancelled;

        public GafiEntityUseEvent(GafiPlayer player, GafiEntity entity) {
            this.player = player;
            this.entity = entity;
        }

        public GafiPlayer player() { return player; }
        public GafiEntity entity() { return entity; }
        public boolean isCancelled() { return cancelled; }
        public void cancel() { cancelled = true; }
    }

    public record GafiServerTickEvent() {}
}
