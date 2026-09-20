package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.GafiScriptMod;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public final class GafiCommandHandle {
    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private final String name;
    private final String ownerScript;
    private volatile boolean registered;

    GafiCommandHandle(
            CommandDispatcher<ServerCommandSource> dispatcher,
            String name,
            String ownerScript
    ) {
        this.dispatcher = dispatcher;
        this.name = name;
        this.ownerScript = ownerScript;
        this.registered = true;
    }

    public String name() {
        return name;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String ownerScript() {
        return ownerScript;
    }

    public void unregister() {
        if (!registered) return;

        /*
         * Brigadier does not expose a public remove-child operation on
         * RootCommandNode in the version used by Minecraft 1.21.11.
         * Marking the handle inactive keeps script lifecycle state correct;
         * the command tree itself is rebuilt by Minecraft on the next command
         * registration/reload.
         */
        registered = false;
    }
}
