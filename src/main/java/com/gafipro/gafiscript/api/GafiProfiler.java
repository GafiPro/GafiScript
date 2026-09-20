package com.gafipro.gafiscript.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class GafiProfiler {
    private final Map<String, Metric> metrics =
            new ConcurrentHashMap<>();

    public <T> T measure(
            String name,
            java.util.function.Supplier<T> operation
    ) {
        long start = System.nanoTime();
        try {
            return operation.get();
        } finally {
            record(name, System.nanoTime() - start);
        }
    }

    public void measure(String name, Runnable operation) {
        long start = System.nanoTime();
        try {
            operation.run();
        } finally {
            record(name, System.nanoTime() - start);
        }
    }

    public MetricSnapshot snapshot(String name) {
        Metric metric = metrics.get(name);
        return metric == null
                ? new MetricSnapshot(name, 0, 0, 0)
                : metric.snapshot(name);
    }

    public Map<String, MetricSnapshot> snapshotAll() {
        return metrics.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().snapshot(entry.getKey())
                ));
    }

    public void reset() {
        metrics.clear();
    }

    private void record(String name, long nanos) {
        metrics.computeIfAbsent(
                name,
                ignored -> new Metric()
        ).record(nanos);
    }

    private static final class Metric {
        private final LongAdder calls = new LongAdder();
        private final LongAdder totalNanos = new LongAdder();
        private volatile long maxNanos;

        void record(long nanos) {
            calls.increment();
            totalNanos.add(nanos);

            synchronized (this) {
                if (nanos > maxNanos) {
                    maxNanos = nanos;
                }
            }
        }

        MetricSnapshot snapshot(String name) {
            long count = calls.sum();
            long total = totalNanos.sum();

            return new MetricSnapshot(
                    name,
                    count,
                    count == 0 ? 0 : total / 1_000_000.0 / count,
                    maxNanos / 1_000_000.0
            );
        }
    }

    public record MetricSnapshot(
            String name,
            long calls,
            double averageMillis,
            double maxMillis
    ) {}
}
