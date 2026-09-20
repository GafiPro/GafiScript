package com.gafipro.gafiscript.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

public final class GafiTask {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile long remainingTicks;
    private final long periodTicks;
    private final Runnable action;
    private final boolean repeating;

    GafiTask(long delayTicks, long periodTicks, Runnable action, boolean repeating) {
        this.remainingTicks = Math.max(0, delayTicks);
        this.periodTicks = Math.max(1, periodTicks);
        this.action = action;
        this.repeating = repeating;
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    boolean tick() {
        if (cancelled.get()) return true;
        if (remainingTicks > 0) {
            remainingTicks--;
            return false;
        }

        try {
            action.run();
        } catch (Throwable throwable) {
            com.gafipro.gafiscript.GafiScriptMod.LOGGER.error("GafiScript scheduled task failed", throwable);
        }

        if (!repeating) {
            cancelled.set(true);
            return true;
        }

        remainingTicks = periodTicks;
        return false;
    }
}
