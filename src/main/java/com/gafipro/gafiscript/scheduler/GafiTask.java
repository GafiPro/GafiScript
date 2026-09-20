package com.gafipro.gafiscript.scheduler;

import com.gafipro.gafiscript.runtime.GafiScriptContext;

import java.util.concurrent.atomic.AtomicBoolean;

public final class GafiTask {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final long periodTicks;
    private final Runnable action;
    private final boolean repeating;
    private final String ownerScript;
    private volatile long remainingTicks;

    GafiTask(
            long delayTicks,
            long periodTicks,
            Runnable action,
            boolean repeating
    ) {
        this.remainingTicks = Math.max(0, delayTicks);
        this.periodTicks = Math.max(1, periodTicks);
        this.action = action;
        this.repeating = repeating;
        this.ownerScript = GafiScriptContext.currentScript();
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public String ownerScript() {
        return ownerScript;
    }

    boolean tick() {
        if (cancelled.get()) return true;

        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks > 0) {
                return false;
            }
        }

        if (ownerScript == null) {
            runAction();
        } else {
            GafiScriptContext.runAs(ownerScript, this::runAction);
        }

        if (!repeating) {
            cancelled.set(true);
            return true;
        }

        remainingTicks = periodTicks;
        return false;
    }

    private void runAction() {
        long started = System.nanoTime();

        try {
            action.run();
        } catch (Throwable throwable) {
            com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                    "GafiScript scheduled task failed",
                    throwable
            );
        } finally {
            if (ownerScript != null) {
                GafiScriptApiWatchdog.observe(
                        ownerScript,
                        System.nanoTime() - started
                );
            }
        }
    }

    private static final class GafiScriptApiWatchdog {
        private static void observe(
                String owner,
                long nanos
        ) {
            try {
                com.gafipro.gafiscript.api.Gafi.watchdog()
                        .observe(owner, nanos);
            } catch (Throwable ignored) {
            }
        }
    }
}
