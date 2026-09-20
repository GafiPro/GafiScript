package com.gafipro.gafiscript.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GafiSequence {
    private final GafiScheduler scheduler;
    private final List<Step> steps = new ArrayList<>();
    private final GafiTaskGroup group = new GafiTaskGroup("sequence");
    private long pendingDelayTicks;

    GafiSequence(GafiScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public GafiSequence then(Runnable action) {
        steps.add(new Step(
                Math.max(0, pendingDelayTicks),
                Objects.requireNonNull(action, "action")
        ));
        pendingDelayTicks = 0;
        return this;
    }

    public GafiSequence delayTicks(long ticks) {
        pendingDelayTicks += Math.max(0, ticks);
        return this;
    }

    public GafiSequence delaySeconds(double seconds) {
        return delayTicks(Math.max(0, Math.round(seconds * 20.0)));
    }

    public void start() {
        if (steps.isEmpty()) {
            return;
        }
        scheduleStep(0);
    }

    public void cancel() {
        group.cancelAll();
    }

    private void scheduleStep(int index) {
        if (index >= steps.size()) {
            return;
        }

        Step step = steps.get(index);
        GafiTask task = scheduler.delayTicks(
                step.delayTicks(),
                () -> {
                    try {
                        step.action().run();
                    } catch (Throwable throwable) {
                        com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                                "GafiScript sequence step failed",
                                throwable
                        );
                        cancel();
                        return;
                    }

                    if (index + 1 < steps.size()) {
                        scheduleStep(index + 1);
                    }
                }
        );

        group.add(task);
    }

    private record Step(long delayTicks, Runnable action) {}
}
