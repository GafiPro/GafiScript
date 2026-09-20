package com.gafipro.gafiscript.api;

import java.util.concurrent.ThreadLocalRandom;

public final class GafiRandom {
    public int nextInt(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("minInclusive must not exceed maxInclusive");
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    public long nextLong(long minInclusive, long maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("minInclusive must not exceed maxInclusive");
        }
        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1);
    }

    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public boolean chance(double probability) {
        if (probability <= 0) return false;
        if (probability >= 1) return true;
        return ThreadLocalRandom.current().nextDouble() < probability;
    }
}
