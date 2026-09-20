package com.gafipro.gafiscript.command;

import com.gafipro.gafiscript.runtime.ScriptManager;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                literal("gafiscript")
                                        .requires(source ->
                                                source.getPermissions()
                                                        .hasPermission(DefaultPermissions.GAMEMASTERS))
                                        .then(literal("help").executes(context -> {
                                            sendHelp(context.getSource());
                                            return 1;
                                        }))
                                        .then(literal("list").executes(context -> {
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(
                                                            "Active scripts: " +
                                                            ScriptManager.activeScripts()
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        }))
                                        .then(literal("run")
                                                .then(argument("script", StringArgumentType.word())
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
                                                        })))
                                        .then(literal("stop")
                                                .then(argument("script", StringArgumentType.word())
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
                                                        })))
                                        .then(literal("info")
                                                .then(argument("script", StringArgumentType.word())
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
                                                        })))
                                        .then(literal("project")
                                                .then(literal("list")
                                                        .executes(context -> {
                                                            var projects =
                                                                    ScriptManager.projects(
                                                                            context.getSource().getServer()
                                                                    );

                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal(
                                                                            "Projects: " + projects
                                                                    ),
                                                                    false
                                                            );
                                                            return 1;
                                                        }))
                                                .then(literal("create")
                                                        .then(argument("project", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    String name =
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "project"
                                                                            );

                                                                    String result =
                                                                            ScriptManager.createProject(
                                                                                    context.getSource().getServer(),
                                                                                    name
                                                                            );

                                                                    context.getSource().sendFeedback(
                                                                            () -> Text.literal(result),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })))
                                                .then(literal("run")
                                                        .then(argument("project", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    String name =
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "project"
                                                                            );

                                                                    ScriptManager.runProjectAsync(
                                                                            context.getSource().getServer(),
                                                                            name
                                                                    );

                                                                    context.getSource().sendFeedback(
                                                                            () -> Text.literal(
                                                                                    "Project run scheduled: " + name
                                                                            ),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })))
                                                .then(literal("reload")
                                                        .then(argument("project", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    String name =
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "project"
                                                                            );

                                                                    String result =
                                                                            ScriptManager.reloadProject(
                                                                                    context.getSource().getServer(),
                                                                                    name
                                                                            );

                                                                    context.getSource().sendFeedback(
                                                                            () -> Text.literal(result),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })))
                                                .then(literal("edit")
                                                        .then(argument("project", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    String name =
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "project"
                                                                            );

                                                                    var player =
                                                                            context.getSource().getPlayer();

                                                                    if (player != null) {
                                                                        com.gafipro.gafiscript.net.GafiScriptNetworking
                                                                                .openProjectFromServer(
                                                                                        player,
                                                                                        name
                                                                                );
                                                                    }

                                                                    return 1;
                                                                })))
                                                .then(literal("export")
                                                        .then(argument("project", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    String name =
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "project"
                                                                            );

                                                                    String result =
                                                                            ScriptManager.exportProject(
                                                                                    context.getSource().getServer(),
                                                                                    name
                                                                            );

                                                                    context.getSource().sendFeedback(
                                                                            () -> Text.literal(result),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })))
                                                .then(literal("import")
                                                        .then(argument("archive", StringArgumentType.string())
                                                                .executes(context -> {
                                                                    String archive =
                                                                            StringArgumentType.getString(
                                                                                    context,
                                                                                    "archive"
                                                                            );

                                                                    String result =
                                                                            ScriptManager.importProject(
                                                                                    context.getSource().getServer(),
                                                                                    archive
                                                                            );

                                                                    context.getSource().sendFeedback(
                                                                            () -> Text.literal(result),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })))
                                        )
                        )
        );
    }

    private static void sendHelp(ServerCommandSource source) {
        source.sendFeedback(
                () -> Text.literal(
                        "/gafiscript help | list | run <script> | stop <script> | info <script> | project <list|create|run|reload|edit|export|import>"
                ),
                false
        );
    }
}
