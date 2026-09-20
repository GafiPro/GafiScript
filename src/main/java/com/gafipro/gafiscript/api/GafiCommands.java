package com.gafipro.gafiscript.api;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;

public final class GafiCommands {
    private final List<GafiCommandHandle> handles =
            java.util.concurrent.CopyOnWriteArrayList<>();

    public GafiCommandHandle register(
            String name,
            Consumer<LiteralArgumentBuilder<ServerCommandSource>> configure
    ) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(configure, "configure");

        String safeName = name.trim();
        if (safeName.isEmpty() ||
                !safeName.matches("[A-Za-z0-9_:-]+")) {
            throw new IllegalArgumentException(
                    "Invalid command name: " + name
            );
        }

        CommandDispatcher<ServerCommandSource> dispatcher =
                Gafi.server()
                        .getCommandManager()
                        .getDispatcher();

        LiteralArgumentBuilder<ServerCommandSource> root =
                literal(safeName);

        configure.accept(root);
        dispatcher.register(root);

        Gafi.server().getPlayerManager().getPlayerList()
                .forEach(player ->
                        Gafi.server().getCommandManager().sendCommandTree(player)
                );

        GafiCommandHandle handle =
                new GafiCommandHandle(
                        dispatcher,
                        safeName,
                        com.gafipro.gafiscript.runtime.GafiScriptContext.currentScript()
                );

        handles.add(handle);
        return handle;
    }

    public void unregisterOwnedBy(String scriptName) {
        handles.removeIf(handle -> {
            if (!java.util.Objects.equals(handle.ownerScript(), scriptName)) {
                return false;
            }
            handle.unregister();
            return true;
        });
    }

    public void unregisterAll() {
        handles.forEach(handle -> {
            try {
                handle.unregister();
            } catch (Throwable throwable) {
                GafiScriptMod.LOGGER.warn(
                        "Failed to unregister GafiScript command {}",
                        handle.name(),
                        throwable
                );
            }
        });
        handles.clear();
    }
}
