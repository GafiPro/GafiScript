package com.gafipro.gafiscript.api;

import java.util.concurrent.atomic.AtomicBoolean;

public final class GafiCustomEventHandle {
    private final String ownerScript;
    private final Runnable remover;
    private final AtomicBoolean registered =
            new AtomicBoolean(true);

    GafiCustomEventHandle(
            String ownerScript,
            Runnable remover
    ) {
        this.ownerScript = ownerScript;
        this.remover = remover;
    }

    public String ownerScript() {
        return ownerScript;
    }

    public boolean isRegistered() {
        return registered.get();
    }

    public void unregister() {
        if (registered.compareAndSet(true, false)) {
            remover.run();
        }
    }
}
