package com.gafipro.gafiscript.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GafiCustomEventsTest {
    @Test
    void emitsPayloadToListener() {
        GafiCustomEvents events =
                new GafiCustomEvents();

        AtomicReference<Object> received =
                new AtomicReference<>();

        var handle =
                events.on(
                        "race.finish",
                        received::set
                );

        assertEquals(
                1,
                events.emit(
                        "race.finish",
                        "Gabriel"
                )
        );

        assertEquals(
                "Gabriel",
                received.get()
        );

        handle.unregister();

        assertEquals(
                0,
                events.emit(
                        "race.finish",
                        "Nobody"
                )
        );
    }
}
