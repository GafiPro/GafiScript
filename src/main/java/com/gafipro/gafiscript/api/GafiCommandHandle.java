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

        dispatcher.getRoot().removeCommand(name);
        registered = false;

        var server = com.gafipro.gafiscript.api.Gafi.server();
        server.getPlayerManager().getPlayerList()
                .forEach(player ->
                        server.getCommandManager().sendCommandTree(player)
                );
    }
}
