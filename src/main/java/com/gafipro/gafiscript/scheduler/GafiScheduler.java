package com.gafipro.gafiscript.scheduler;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GafiScheduler {
    private final List<GafiTask> tasks = new ArrayList<>();
    private volatile ExecutorService asyncExecutor =
            Executors.newVirtualThreadPerTaskExecutor();
    private volatile MinecraftServer server;

    public void attach(MinecraftServer server) {
        this.server = server;

        if (asyncExecutor.isShutdown() || asyncExecutor.isTerminated()) {
            asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
        }
    }

    public void detach() {
        synchronized (tasks) {
            tasks.forEach(GafiTask::cancel);
            tasks.clear();
        }

        asyncExecutor.shutdownNow();
        server = null;
    }

    public GafiTask nextTick(Runnable action) {
        return schedule(1, 1, action, false);
    }

    public GafiTask delayTicks(long ticks, Runnable action) {
        return schedule(Math.max(0, ticks), 1, action, false);
    }

    public GafiTask delaySeconds(double seconds, Runnable action) {
        return delayTicks(Math.max(0, Math.round(seconds * 20.0)), action);
    }

    public GafiTask repeatTicks(long periodTicks, Runnable action) {
        long period = Math.max(1, periodTicks);
        return schedule(period, period, action, true);
    }

    public GafiTask repeatSeconds(double seconds, Runnable action) {
        return repeatTicks(Math.max(1, Math.round(seconds * 20.0)), action);
    }

    public GafiTaskGroup group(String name) {
        return new GafiTaskGroup(name);
    }

    public GafiSequence sequence() {
        return new GafiSequence(this);
    }

    public void runSync(Runnable action) {
        Objects.requireNonNull(action, "action");
        requireServer().execute(action);
    }

    public CompletableFuture<Void> runAsync(Runnable action) {
        Objects.requireNonNull(action, "action");
        return CompletableFuture.runAsync(action, asyncExecutor);
    }

    public <T> CompletableFuture<T> supplyAsync(
            java.util.function.Supplier<T> supplier
    ) {
        Objects.requireNonNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(supplier, asyncExecutor);
    }

    public void tick() {
        List<GafiTask> snapshot;

        synchronized (tasks) {
            snapshot = List.copyOf(tasks);
        }

        List<GafiTask> completed = new ArrayList<>();

        for (GafiTask task : snapshot) {
            try {
                if (task.tick()) {
                    completed.add(task);
                }
            } catch (Throwable throwable) {
                com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                        "GafiScript task failed outside its own error handler",
                        throwable
                );
                completed.add(task);
            }
        }

        if (!completed.isEmpty()) {
            synchronized (tasks) {
                tasks.removeAll(completed);
            }
        }
    }

    private GafiTask schedule(
            long delayTicks,
            long periodTicks,
            Runnable action,
            boolean repeating
    ) {
        Objects.requireNonNull(action, "action");
        requireServer();

        GafiTask task = new GafiTask(
                delayTicks,
                periodTicks,
                action,
                repeating
        );

        synchronized (tasks) {
            tasks.add(task);
        }

        return task;
    }

    private MinecraftServer requireServer() {
        MinecraftServer current = server;

        if (current == null) {
            throw new IllegalStateException(
                    "GafiScript scheduler is not attached to a server."
            );
        }

        return current;
    }
}
