package com.gafipro.gafiscript.command;

import com.gafipro.gafiscript.runtime.ScriptManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GafiScriptCommands {
    private GafiScriptCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                literal("gafiscript")
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .then(
                                                literal("help")
                                                        .executes(context -> {
                                                            sendHelp(context.getSource());
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                literal("list")
                                                        .executes(context -> {
                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal(
                                                                            "Active scripts: " +
                                                                            ScriptManager.activeScripts()
                                                                    ),
                                                                    false
                                                            );
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                literal("run")
                                                        .then(
                                                                argument("script", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            String name =
                                                                                    StringArgumentType.getString(
                                                                                            context,
                                                                                            "script"
                                                                                    );

                                                                            String result =
                                                                                    ScriptManager.reloadFromFile(
                                                                                            context.getSource().getServer(),
                                                                                            name
                                                                                    );

                                                                            context.getSource().sendFeedback(
                                                                                    () -> Text.literal(result),
                                                                                    false
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                literal("stop")
                                                        .then(
                                                                argument("script", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            String name =
                                                                                    StringArgumentType.getString(
                                                                                            context,
                                                                                            "script"
                                                                                    );

                                                                            context.getSource().sendFeedback(
                                                                                    () -> Text.literal(
                                                                                            ScriptManager.stop(name)
                                                                                    ),
                                                                                    false
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                literal("info")
                                                        .then(
                                                                argument("script", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            String name =
                                                                                    StringArgumentType.getString(
                                                                                            context,
                                                                                            "script"
                                                                                    );

                                                                            boolean active =
                                                                                    ScriptManager.activeScripts()
                                                                                            .contains(name);

                                                                            context.getSource().sendFeedback(
                                                                                    () -> Text.literal(
                                                                                            name + ": " +
                                                                                            (active
                                                                                                    ? "RUNNING"
                                                                                                    : "STOPPED")
                                                                                    ),
                                                                                    false
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    private static void sendHelp(ServerCommandSource source) {
        source.sendFeedback(
                () -> Text.literal(
                        "/gafiscript help | list | run <script> | stop <script> | info <script>"
                ),
                false
        );
    }
}
