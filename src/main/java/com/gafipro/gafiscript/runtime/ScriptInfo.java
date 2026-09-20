package com.gafipro.gafiscript.runtime;

import java.time.Instant;
import java.util.Objects;

public record ScriptInfo(
        String name,
        ScriptState state,
        Instant startedAt,
        Instant lastChangedAt,
        String lastMessage
) {
    public ScriptInfo {
        Objects.requireNonNull(
                name,
                "name"
        );
        Objects.requireNonNull(
                state,
                "state"
        );
        Objects.requireNonNull(
                lastChangedAt,
                "lastChangedAt"
        );

        lastMessage =
                lastMessage == null
                        ? ""
                        : lastMessage;
    }
}
