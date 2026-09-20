package com.gafipro.gafiscript.api;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class GafiDebugger {
    private static final Map<String, Set<Integer>> BREAKPOINTS =
            new ConcurrentHashMap<>();

    private static final Map<String, ArrayDeque<DebugHit>> HITS =
            new ConcurrentHashMap<>();

    private static final int MAX_HITS = 256;

    private static volatile boolean enabled;

    private GafiDebugger() {}

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void breakpoint(
            String script,
            int line
    ) {
        BREAKPOINTS
                .computeIfAbsent(
                        script,
                        ignored ->
                                ConcurrentHashMap.newKeySet()
                )
                .add(line);
    }

    public static void clearBreakpoint(
            String script,
            int line
    ) {
        Set<Integer> lines =
                BREAKPOINTS.get(script);

        if (lines == null) return;

        lines.remove(line);

        if (lines.isEmpty()) {
            BREAKPOINTS.remove(script);
        }
    }

    public static void clearBreakpoints(
            String script
    ) {
        BREAKPOINTS.remove(script);
    }

    public static Set<Integer> breakpoints(
            String script
    ) {
        return Set.copyOf(
                BREAKPOINTS.getOrDefault(
                        script,
                        Set.of()
                )
        );
    }

    public static List<DebugHit> recentHits(
            String script
    ) {
        ArrayDeque<DebugHit> queue =
                HITS.get(script);

        return queue == null
                ? List.of()
                : List.copyOf(queue);
    }

    public static void clearHits(
            String script
    ) {
        HITS.remove(script);
    }

    /**
     * Called by compiler-injected probes.
     * This debugger is intentionally cooperative: a breakpoint records a hit
     * and notifies listeners, while the script keeps running unless a listener
     * requests cancellation through the returned DebugHit.
     */
    public static DebugHit check(
            String script,
            int line
    ) {
        if (!enabled) {
            return null;
        }

        Set<Integer> lines =
                BREAKPOINTS.get(script);

        boolean breakpoint =
                lines != null &&
                        lines.contains(line);

        if (!breakpoint) {
            return null;
        }

        DebugHit hit =
                new DebugHit(
                        script,
                        line,
                        Thread.currentThread().getName(),
                        Instant.now().toString()
                );

        ArrayDeque<DebugHit> queue =
                HITS.computeIfAbsent(
                        script,
                        ignored ->
                                new ArrayDeque<>()
                );

        synchronized (queue) {
            queue.addLast(hit);

            while (queue.size() >
                    MAX_HITS) {
                queue.removeFirst();
            }
        }

        return hit;
    }

    public record DebugHit(
            String script,
            int line,
            String thread,
            String timestamp
    ) {}
}
