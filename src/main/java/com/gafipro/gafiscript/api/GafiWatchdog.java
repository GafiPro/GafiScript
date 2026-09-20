package com.gafipro.gafiscript.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class GafiWatchdog {
    private static final long DEFAULT_BUDGET_NANOS =
            25_000_000L;

    private final Map<String, Metric> metrics =
            new ConcurrentHashMap<>();

    private volatile long budgetNanos =
            DEFAULT_BUDGET_NANOS;

    public void budgetMillis(
            double milliseconds
    ) {
        budgetNanos =
                Math.max(
                        1L,
                        (long) (
                                milliseconds *
                                        1_000_000.0
                        )
                );
    }

    public long budgetMillis() {
        return budgetNanos / 1_000_000L;
    }

    public void observe(
            String ownerScript,
            long durationNanos
    ) {
        if (ownerScript == null) {
            return;
        }

        Metric metric =
                metrics.computeIfAbsent(
                        ownerScript,
                        ignored -> new Metric()
                );

        metric.calls.increment();

        if (durationNanos > budgetNanos) {
            metric.overBudget.increment();
        }

        metric.totalNanos.add(
                durationNanos
        );

        synchronized (metric) {
            if (durationNanos >
                    metric.maxNanos) {
                metric.maxNanos =
                        durationNanos;
            }
        }
    }

    public Snapshot snapshot(
            String ownerScript
    ) {
        Metric metric =
                metrics.get(ownerScript);

        if (metric == null) {
            return new Snapshot(
                    ownerScript,
                    0,
                    0,
                    0,
                    0
            );
        }

        long calls =
                metric.calls.sum();

        return new Snapshot(
                ownerScript,
                calls,
                metric.overBudget.sum(),
                calls == 0
                        ? 0
                        : metric.totalNanos.sum()
                                / 1_000_000.0
                                / calls,
                metric.maxNanos /
                        1_000_000.0
        );
    }

    public Map<String, Snapshot> snapshotAll() {
        return metrics.keySet()
                .stream()
                .collect(
                        java.util.stream.Collectors
                                .toUnmodifiableMap(
                                        key -> key,
                                        this::snapshot
                                )
                );
    }

    public void reset() {
        metrics.clear();
    }

    private static final class Metric {
        final LongAdder calls =
                new LongAdder();
        final LongAdder overBudget =
                new LongAdder();
        final LongAdder totalNanos =
                new LongAdder();

        volatile long maxNanos;
    }

    public record Snapshot(
            String script,
            long calls,
            long overBudgetCalls,
            double averageMillis,
            double maxMillis
    ) {}
}
