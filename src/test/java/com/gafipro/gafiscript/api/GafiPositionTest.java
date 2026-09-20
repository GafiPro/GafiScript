package com.gafipro.gafiscript.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GafiPositionTest {
    @Test
    void supportsAddition() {
        GafiPosition original = new GafiPosition(1, 2, 3);
        assertEquals(new GafiPosition(6, 0, 5), original.add(5, -2, 2));
    }

    @Test
    void calculatesDistance() {
        assertEquals(
                5.0,
                new GafiPosition(0, 0, 0).distanceTo(new GafiPosition(3, 4, 0)),
                0.00001
        );
    }
}
