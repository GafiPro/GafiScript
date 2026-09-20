
package com.gafipro.gafiscript.command;

import com.gafipro.gafiscript.api.Gafi;
import com.gafipro.gafiscript.api.GafiDebugger;
import com.gafipro.gafiscript.api.GafiRepl;
import com.gafipro.gafiscript.runtime.ScriptManager;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GafiScriptCommands {
    private GafiScriptCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    LiteralArgumentBuilder<ServerCommandSource> root =
                            literal("gafiscript")
                                    .requires(source ->
                                            source.getPermissions()
                                                    .hasPermission(
                                                            DefaultPermissions.GAMEMASTERS
                                                    )
                                    );

                    root.then(
                            literal("help")
                                    .executes(context -> {
                                        sendHelp(context.getSource());
                                        return 1;
                                    })
                    );

                    root.then(
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
                    );

                    root.then(
                            literal("run")
                                    .then(
                                            argument(
                                                    "script",
                                                    StringArgumentType.word()
                                            ).executes(context -> {
                                                String name =
                                                        StringArgumentType.getString(
                                                                context,
                                                                "script"
                                                        );

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                ScriptManager.reloadFromFile(
                                                                        context.getSource().getServer(),
                                                                        name
                                                                )
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                    );

                    root.then(
                            literal("stop")
                                    .then(
                                            argument(
                                                    "script",
                                                    StringArgumentType.word()
                                            ).executes(context -> {
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
                    );

                    root.then(
                            literal("info")
                                    .then(
                                            argument(
                                                    "script",
                                                    StringArgumentType.word()
                                            ).executes(context -> {
                                                String name =
                                                        StringArgumentType.getString(
                                                                context,
                                                                "script"
                                                        );

                                                var info =
                                                        ScriptManager.info(name);

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                info.name() +
                                                                        ": " +
                                                                        info.state() +
                                                                        " | " +
                                                                        info.lastMessage()
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                    );

                    root.then(replCommand());
                    root.then(debugCommand());
                    root.then(watchdogCommand());
                    root.then(customEventCommand());
                    root.then(projectCommand());

                    dispatcher.register(root);
                }
        );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> replCommand() {
        return literal("repl")
                .then(
                        argument(
                                "code",
                                StringArgumentType.greedyString()
                        ).executes(context -> {
                            ServerCommandSource source =
                                    context.getSource();

                            String code =
                                    StringArgumentType.getString(
                                            context,
                                            "code"
                                    );

                            GafiRepl.evaluate(
                                            source.getServer(),
                                            source.getName(),
                                            code
                                    )
                                    .thenAccept(result ->
                                            source.getServer().execute(
                                                    () -> source.sendFeedback(
                                                            () -> Text.literal(
                                                                    "[REPL] " +
                                                                            result
                                                            ),
                                                            false
                                                    )
                                            )
                                    );

                            source.sendFeedback(
                                    () -> Text.literal(
                                            "REPL evaluation scheduled."
                                    ),
                                    false
                            );

                            return 1;
                        })
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> debugCommand() {
        return literal("debug")
                .then(
                        literal("enable")
                                .executes(context -> {
                                    GafiDebugger.enable();
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    "Debugger enabled."
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                )
                .then(
                        literal("disable")
                                .executes(context -> {
                                    GafiDebugger.disable();
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    "Debugger disabled."
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                )
                .then(
                        literal("break")
                                .then(
                                        argument(
                                                "script",
                                                StringArgumentType.word()
                                        ).then(
                                                argument(
                                                        "line",
                                                        IntegerArgumentType.integer(1)
                                                ).executes(context -> {
                                                    String script =
                                                            StringArgumentType.getString(
                                                                    context,
                                                                    "script"
                                                            );

                                                    int line =
                                                            IntegerArgumentType.getInteger(
                                                                    context,
                                                                    "line"
                                                            );

                                                    GafiDebugger.breakpoint(
                                                            script,
                                                            line
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                )
                .then(
                        literal("clear")
                                .then(
                                        argument(
                                                "script",
                                                StringArgumentType.word()
                                        ).then(
                                                argument(
                                                        "line",
                                                        IntegerArgumentType.integer(1)
                                                ).executes(context -> {
                                                    String script =
                                                            StringArgumentType.getString(
                                                                    context,
                                                                    "script"
                                                            );

                                                    int line =
                                                            IntegerArgumentType.getInteger(
                                                                    context,
                                                                    "line"
                                                            );

                                                    GafiDebugger.clearBreakpoint(
                                                            script,
                                                            line
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                )
                .then(
                        literal("hits")
                                .then(
                                        argument(
                                                "script",
                                                StringArgumentType.word()
                                        ).executes(context -> {
                                            String script =
                                                    StringArgumentType.getString(
                                                            context,
                                                            "script"
                                                    );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(
                                                            "Debugger hits: " +
                                                                    GafiDebugger.recentHits(script)
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> watchdogCommand() {
        return literal("watchdog")
                .then(
                        literal("budget")
                                .then(
                                        argument(
                                                "milliseconds",
                                                DoubleArgumentType.doubleArg(1.0)
                                        ).executes(context -> {
                                            double value =
                                                    DoubleArgumentType.getDouble(
                                                            context,
                                                            "milliseconds"
                                                    );

                                            Gafi.watchdog()
                                                    .budgetMillis(value);

                                            return 1;
                                        })
                                )
                )
                .then(
                        literal("show")
                                .executes(context -> {
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    String.valueOf(
                                                            Gafi.watchdog()
                                                                    .snapshotAll()
                                                    )
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> customEventCommand() {
        return literal("event")
                .then(
                        argument(
                                "name",
                                StringArgumentType.word()
                        ).then(
                                argument(
                                        "payload",
                                        StringArgumentType.greedyString()
                                ).executes(context -> {
                                    String name =
                                            StringArgumentType.getString(
                                                    context,
                                                    "name"
                                            );

                                    String payload =
                                            StringArgumentType.getString(
                                                    context,
                                                    "payload"
                                            );

                                    int count =
                                            Gafi.customEvents()
                                                    .emit(
                                                            name,
                                                            payload
                                                    );

                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    "Custom event emitted to " +
                                                            count +
                                                            " listener(s)."
                                            ),
                                            false
                                    );

                                    return 1;
                                })
                        )
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> projectCommand() {
        LiteralArgumentBuilder<ServerCommandSource> project =
                literal("project");

        project.then(
                literal("list")
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    () -> Text.literal(
                                            "Projects: " +
                                                    ScriptManager.projects(
                                                            context.getSource().getServer()
                                                    )
                                    ),
                                    false
                            );
                            return 1;
                        })
        );

        project.then(
                literal("create")
                        .then(
                                argument(
                                        "project",
                                        StringArgumentType.word()
                                ).executes(context -> {
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    ScriptManager.createProject(
                                                            context.getSource().getServer(),
                                                            StringArgumentType.getString(
                                                                    context,
                                                                    "project"
                                                            )
                                                    )
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );

        project.then(
                literal("run")
                        .then(
                                argument(
                                        "project",
                                        StringArgumentType.word()
                                ).executes(context -> {
                                    String name =
                                            StringArgumentType.getString(
                                                    context,
                                                    "project"
                                            );

                                    ScriptManager.runProjectAsync(
                                            context.getSource().getServer(),
                                            name
                                    );

                                    return 1;
                                })
                        )
        );

        project.then(
                literal("reload")
                        .then(
                                argument(
                                        "project",
                                        StringArgumentType.word()
                                ).executes(context -> {
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    ScriptManager.reloadProject(
                                                            context.getSource().getServer(),
                                                            StringArgumentType.getString(
                                                                    context,
                                                                    "project"
                                                            )
                                                    )
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );

        project.then(
                literal("edit")
                        .then(
                                argument(
                                        "project",
                                        StringArgumentType.word()
                                ).executes(context -> {
                                    var player =
                                            context.getSource().getPlayer();

                                    if (player != null) {
                                        com.gafipro.gafiscript.net.GafiScriptNetworking
                                                .openProjectFromServer(
                                                        player,
                                                        StringArgumentType.getString(
                                                                context,
                                                                "project"
                                                        )
                                                );
                                    }

                                    return 1;
                                })
                        )
        );

        project.then(
                literal("export")
                        .then(
                                argument(
                                        "project",
                                        StringArgumentType.word()
                                ).executes(context -> {
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    ScriptManager.exportProject(
                                                            context.getSource().getServer(),
                                                            StringArgumentType.getString(
                                                                    context,
                                                                    "project"
                                                            )
                                                    )
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );

        project.then(
                literal("import")
                        .then(
                                argument(
                                        "archive",
                                        StringArgumentType.string()
                                ).executes(context -> {
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    ScriptManager.importProject(
                                                            context.getSource().getServer(),
                                                            StringArgumentType.getString(
                                                                    context,
                                                                    "archive"
                                                            )
                                                    )
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );

        return project;
    }

    private static void sendHelp(ServerCommandSource source) {
        source.sendFeedback(
                () -> Text.literal(
                        "/gafiscript help | list | run <script> | stop <script> | info <script> | project <list|create|run|reload|edit|export|import> | repl <code> | debug <enable|disable|break|clear|hits> | watchdog <budget|show> | event <name> <payload>"
                ),
                false
        );
    }
}
