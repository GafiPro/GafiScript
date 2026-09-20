package com.gafipro.gafiscript.scheduler;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GafiTaskTest {
    @Test
    void oneTickDelayRunsOnFirstSchedulerTick() {
        AtomicInteger calls = new AtomicInteger();

        GafiTask task =
                new GafiTask(
                        1,
                        1,
                        calls::incrementAndGet,
                        false
                );

        assertTrue(task.tick());
        assertEquals(1, calls.get());
        assertTrue(task.isCancelled());
    }

    @Test
    void zeroTickDelayRunsOnFirstSchedulerTick() {
        AtomicInteger calls = new AtomicInteger();

        GafiTask task =
                new GafiTask(
                        0,
                        1,
                        calls::incrementAndGet,
                        false
                );

        assertTrue(task.tick());
        assertEquals(1, calls.get());
    }

    @Test
    void repeatingTaskRunsAtItsConfiguredPeriod() {
        AtomicInteger calls = new AtomicInteger();

        GafiTask task =
                new GafiTask(
                        2,
                        2,
                        calls::incrementAndGet,
                        true
                );

        assertFalse(task.tick());
        assertEquals(0, calls.get());

        assertTrueOrNotCancelled(task, calls, 1);
        assertEquals(1, calls.get());

        assertFalse(task.tick());
        assertEquals(1, calls.get());

        assertTrueOrNotCancelled(task, calls, 1);
        assertEquals(2, calls.get());
        assertFalse(task.isCancelled());
    }

    private static void assertTrueOrNotCancelled(
            GafiTask task,
            AtomicInteger calls,
            int expectedIncrement
    ) {
        boolean completed = task.tick();
        assertFalse(completed, "repeating task must remain scheduled");
        assertEquals(expectedIncrement, calls.get());
    }
}
