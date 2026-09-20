package com.gafipro.gafiscript.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GafiDebuggerTest {
    @AfterEach
    void reset() {
        GafiDebugger.disable();
        GafiDebugger.clearBreakpoints("Test");
        GafiDebugger.clearHits("Test");
    }

    @Test
    void recordsConfiguredBreakpointHit() {
        GafiDebugger.enable();
        GafiDebugger.breakpoint("Test", 7);

        GafiDebugger.DebugHit hit =
                GafiDebugger.check("Test", 7);

        assertNotNull(hit);
        assertEquals(7, hit.line());
        assertEquals(1, GafiDebugger.recentHits("Test").size());
    }

    @Test
    void ignoresLinesWithoutBreakpoint() {
        GafiDebugger.enable();
        GafiDebugger.breakpoint("Test", 7);

        assertNull(GafiDebugger.check("Test", 8));
    }
}
