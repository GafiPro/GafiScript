package com.gafipro.gafiscript.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ScriptStateStoreTest {
    @Test
    void splitsDottedKeys() {
        assertArrayEquals(
                new String[]{"players", "Gabriel", "wins"},
                ScriptStateStore.splitKey("players.Gabriel.wins")
        );
    }

    @Test
    void blankKeyProducesNoParts() {
        assertArrayEquals(
                new String[0],
                ScriptStateStore.splitKey(" ")
        );
    }
}
