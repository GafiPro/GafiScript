package com.gafipro.gafiscript.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GafiWatchdogTest {
    @Test
    void tracksOverBudgetCalls() {
        GafiWatchdog watchdog =
                new GafiWatchdog();

        watchdog.budgetMillis(1);

        watchdog.observe(
                "Test",
                2_000_000L
        );

        var snapshot =
                watchdog.snapshot("Test");

        assertEquals(
                1,
                snapshot.calls()
        );

        assertEquals(
                1,
                snapshot.overBudgetCalls()
        );
    }
}
