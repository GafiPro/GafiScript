package com.gafipro.gafiscript.api;

import com.mojang.brigadier.tree.CommandNode;

import java.lang.reflect.Field;
import java.util.Map;

public final class GafiCommandHandle {
    private static final Field CHILDREN;
    private static final Field LITERALS;
    private static final Field ARGUMENTS;

    static {
        try {
            CHILDREN =
                    CommandNode.class.getDeclaredField(
                            "children"
                    );
            LITERALS =
                    CommandNode.class.getDeclaredField(
                            "literals"
                    );
            ARGUMENTS =
                    CommandNode.class.getDeclaredField(
                            "arguments"
                    );

            CHILDREN.setAccessible(true);
            LITERALS.setAccessible(true);
            ARGUMENTS.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(
                    exception
            );
        }
    }

    private final com.mojang.brigadier.CommandDispatcher<
            net.minecraft.server.command.ServerCommandSource> dispatcher;

    private final String name;
    private final String ownerScript;

    private volatile boolean registered;

    GafiCommandHandle(
            com.mojang.brigadier.CommandDispatcher<
                    net.minecraft.server.command.ServerCommandSource> dispatcher,
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
        if (!registered) {
            return;
        }

        CommandNode<
                net.minecraft.server.command.ServerCommandSource> node =
                dispatcher
                        .getRoot()
                        .getChild(name);

        if (node != null) {
            try {
                removeFromMap(
                        CHILDREN,
                        dispatcher.getRoot(),
                        name,
                        node
                );

                if (node instanceof
                        com.mojang.brigadier.tree.LiteralCommandNode) {
                    removeFromMap(
                            LITERALS,
                            dispatcher.getRoot(),
                            name,
                            node
                    );
                }

                if (node instanceof
                        com.mojang.brigadier.tree.ArgumentCommandNode) {
                    removeFromMap(
                            ARGUMENTS,
                            dispatcher.getRoot(),
                            name,
                            node
                    );
                }
            } catch (ReflectiveOperationException exception) {
                com.gafipro.gafiscript.GafiScriptMod.LOGGER.warn(
                        "Could not remove GafiScript command '{}': {}",
                        name,
                        exception.getMessage()
                );
            }
        }

        registered = false;

        var server =
                com.gafipro.gafiscript.api.Gafi.server();

        server.getPlayerManager()
                .getPlayerList()
                .forEach(
                        player ->
                                server.getCommandManager()
                                        .sendCommandTree(player)
                );
    }

    @SuppressWarnings("unchecked")
    private static void removeFromMap(
            Field field,
            CommandNode<
                    net.minecraft.server.command.ServerCommandSource> root,
            String name,
            CommandNode<
                    net.minecraft.server.command.ServerCommandSource> node
    ) throws IllegalAccessException {
        Map<String, CommandNode<
                net.minecraft.server.command.ServerCommandSource>> map =
                (Map<String, CommandNode<
                        net.minecraft.server.command.ServerCommandSource>>)
                        field.get(root);

        map.remove(name, node);
    }
}
