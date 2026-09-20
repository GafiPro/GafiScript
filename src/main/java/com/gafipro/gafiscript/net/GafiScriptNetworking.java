package com.gafipro.gafiscript.net;

import com.gafipro.gafiscript.GafiScriptMod;
import com.gafipro.gafiscript.block.GafiScriptBlockEntity;
import com.gafipro.gafiscript.runtime.ScriptManager;
import com.gafipro.gafiscript.runtime.ScriptProjects;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class GafiScriptNetworking {
    private static final Identifier REQUEST_ID = GafiScriptMod.id("request_script");
    private static final Identifier SAVE_ID = GafiScriptMod.id("save_script");
    private static final Identifier RUN_ID = GafiScriptMod.id("run_script");
    private static final Identifier SCRIPT_DATA_ID = GafiScriptMod.id("script_data");
    private static final Identifier MESSAGE_ID = GafiScriptMod.id("message");

    private static final Identifier PROJECT_OPEN_ID = GafiScriptMod.id("project_open");
    private static final Identifier PROJECT_SAVE_ID = GafiScriptMod.id("project_save");
    private static final Identifier PROJECT_RUN_ID = GafiScriptMod.id("project_run");
    private static final Identifier PROJECT_DATA_ID = GafiScriptMod.id("project_data");

    public record RequestScriptPayload(BlockPos pos) implements CustomPayload {
        public static final Id<RequestScriptPayload> ID = new Id<>(REQUEST_ID);
        public static final PacketCodec<RegistryByteBuf, RequestScriptPayload> CODEC =
                PacketCodec.tuple(BlockPos.PACKET_CODEC, RequestScriptPayload::pos, RequestScriptPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record SaveScriptPayload(BlockPos pos, String name, String source) implements CustomPayload {
        public static final Id<SaveScriptPayload> ID = new Id<>(SAVE_ID);
        public static final PacketCodec<RegistryByteBuf, SaveScriptPayload> CODEC =
                PacketCodec.tuple(
                        BlockPos.PACKET_CODEC, SaveScriptPayload::pos,
                        PacketCodecs.STRING, SaveScriptPayload::name,
                        PacketCodecs.STRING, SaveScriptPayload::source,
                        SaveScriptPayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record RunScriptPayload(BlockPos pos, String name, String source) implements CustomPayload {
        public static final Id<RunScriptPayload> ID = new Id<>(RUN_ID);
        public static final PacketCodec<RegistryByteBuf, RunScriptPayload> CODEC =
                PacketCodec.tuple(
                        BlockPos.PACKET_CODEC, RunScriptPayload::pos,
                        PacketCodecs.STRING, RunScriptPayload::name,
                        PacketCodecs.STRING, RunScriptPayload::source,
                        RunScriptPayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ScriptDataPayload(BlockPos pos, String name, String source) implements CustomPayload {
        public static final Id<ScriptDataPayload> ID = new Id<>(SCRIPT_DATA_ID);
        public static final PacketCodec<RegistryByteBuf, ScriptDataPayload> CODEC =
                PacketCodec.tuple(
                        BlockPos.PACKET_CODEC, ScriptDataPayload::pos,
                        PacketCodecs.STRING, ScriptDataPayload::name,
                        PacketCodecs.STRING, ScriptDataPayload::source,
                        ScriptDataPayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record MessagePayload(String message) implements CustomPayload {
        public static final Id<MessagePayload> ID = new Id<>(MESSAGE_ID);
        public static final PacketCodec<RegistryByteBuf, MessagePayload> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, MessagePayload::message, MessagePayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ProjectOpenPayload(String project, String file) implements CustomPayload {
        public static final Id<ProjectOpenPayload> ID = new Id<>(PROJECT_OPEN_ID);
        public static final PacketCodec<RegistryByteBuf, ProjectOpenPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, ProjectOpenPayload::project,
                        PacketCodecs.STRING, ProjectOpenPayload::file,
                        ProjectOpenPayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ProjectSavePayload(String project, String file, String source) implements CustomPayload {
        public static final Id<ProjectSavePayload> ID = new Id<>(PROJECT_SAVE_ID);
        public static final PacketCodec<RegistryByteBuf, ProjectSavePayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, ProjectSavePayload::project,
                        PacketCodecs.STRING, ProjectSavePayload::file,
                        PacketCodecs.STRING, ProjectSavePayload::source,
                        ProjectSavePayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ProjectRunPayload(String project) implements CustomPayload {
        public static final Id<ProjectRunPayload> ID = new Id<>(PROJECT_RUN_ID);
        public static final PacketCodec<RegistryByteBuf, ProjectRunPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, ProjectRunPayload::project,
                        ProjectRunPayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ProjectDataPayload(
            String project,
            String file,
            String files,
            String source
    ) implements CustomPayload {
        public static final Id<ProjectDataPayload> ID = new Id<>(PROJECT_DATA_ID);
        public static final PacketCodec<RegistryByteBuf, ProjectDataPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, ProjectDataPayload::project,
                        PacketCodecs.STRING, ProjectDataPayload::file,
                        PacketCodecs.STRING, ProjectDataPayload::files,
                        PacketCodecs.STRING, ProjectDataPayload::source,
                        ProjectDataPayload::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    private GafiScriptNetworking() {}

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(RequestScriptPayload.ID, RequestScriptPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveScriptPayload.ID, SaveScriptPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RunScriptPayload.ID, RunScriptPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProjectOpenPayload.ID, ProjectOpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProjectSavePayload.ID, ProjectSavePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProjectRunPayload.ID, ProjectRunPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ScriptDataPayload.ID, ScriptDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MessagePayload.ID, MessagePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ProjectDataPayload.ID, ProjectDataPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestScriptPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!canEdit(player)) return;

            if (!(player.getCommandSource().getWorld().getBlockEntity(payload.pos()) instanceof GafiScriptBlockEntity blockEntity)) {
                return;
            }

            ServerPlayNetworking.send(
                    player,
                    new ScriptDataPayload(payload.pos(), blockEntity.getScriptName(), blockEntity.getSource())
            );
        });

        ServerPlayNetworking.registerGlobalReceiver(SaveScriptPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!canEdit(player)) return;
            saveBlock(player, payload.pos(), payload.name(), payload.source());
        });

        ServerPlayNetworking.registerGlobalReceiver(RunScriptPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!canEdit(player)) return;
            if (!saveBlock(player, payload.pos(), payload.name(), payload.source())) return;

            ScriptManager.runSourceAsync(
                    player.getCommandSource().getServer(),
                    payload.name(),
                    payload.source()
            ).whenComplete((result, throwable) ->
                    player.getCommandSource().getServer().execute(() -> {
                        String message =
                                throwable == null
                                        ? result
                                        : "Runtime failure: " + throwable.getMessage();

                        ServerPlayNetworking.send(
                                player,
                                new MessagePayload(message)
                        );
                    })
            );
        });

        ServerPlayNetworking.registerGlobalReceiver(ProjectOpenPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!canEdit(player)) return;

            sendProjectFile(
                    player,
                    payload.project(),
                    payload.file()
            );
        });

        ServerPlayNetworking.registerGlobalReceiver(ProjectSavePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!canEdit(player)) return;

            String error = saveProjectFile(
                    player,
                    payload.project(),
                    payload.file(),
                    payload.source()
            );

            if (error != null) {
                sendMessage(player, error);
            } else {
                sendProjectFile(
                        player,
                        payload.project(),
                        payload.file()
                );
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ProjectRunPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!canEdit(player)) return;

            ScriptManager.runProjectAsync(
                    player.getCommandSource().getServer(),
                    payload.project()
            ).whenComplete((result, throwable) ->
                    player.getCommandSource().getServer().execute(() ->
                            sendMessage(
                                    player,
                                    throwable == null
                                            ? result
                                            : "Project runtime failure: " +
                                            throwable.getMessage()
                            )
                    )
            );
        });
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ScriptDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof com.gafipro.gafiscript.client.GafiScriptScreen screen) {
                    screen.setScriptData(payload.pos(), payload.name(), payload.source());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MessagePayload.ID, (payload, context) -> {
            context.client().execute(() ->
                    context.client().inGameHud.getChatHud().addMessage(
                            Text.literal("[GafiScript] " + payload.message())
                    ));
        });

        ClientPlayNetworking.registerGlobalReceiver(ProjectDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof com.gafipro.gafiscript.client.GafiProjectScreen screen) {
                    screen.setProjectData(
                            payload.project(),
                            payload.file(),
                            payload.files(),
                            payload.source()
                    );
                } else {
                    context.client().setScreen(
                            new com.gafipro.gafiscript.client.GafiProjectScreen(
                                    payload.project(),
                                    payload.file(),
                                    payload.files(),
                                    payload.source()
                            )
                    );
                }
            });
        });
    }

    public static void sendScriptRequest(BlockPos pos) {
        ClientPlayNetworking.send(new RequestScriptPayload(pos));
    }

    public static void sendSave(BlockPos pos, String name, String source) {
        ClientPlayNetworking.send(new SaveScriptPayload(pos, name, source));
    }

    public static void sendRun(BlockPos pos, String name, String source) {
        ClientPlayNetworking.send(new RunScriptPayload(pos, name, source));
    }

    public static void sendProjectOpen(String project, String file) {
        ClientPlayNetworking.send(new ProjectOpenPayload(project, file));
    }

    public static void sendProjectSave(String project, String file, String source) {
        ClientPlayNetworking.send(new ProjectSavePayload(project, file, source));
    }

    public static void sendProjectRun(String project) {
        ClientPlayNetworking.send(new ProjectRunPayload(project));
    }

    public static void openProjectFromServer(
            ServerPlayerEntity player,
            String project
    ) {
        if (!canEdit(player)) return;

        String file = "Main.java";
        sendProjectData(
                player,
                project,
                file
        );
    }

    private static void sendProjectFile(
            ServerPlayerEntity player,
            String project,
            String file
    ) {
        Path path = projectFile(
                player,
                project,
                file
        );

        if (path == null || !Files.isRegularFile(path)) {
            sendMessage(
                    player,
                    "Project file not found."
            );
            return;
        }

        try {
            String source =
                    Files.readString(
                            path,
                            StandardCharsets.UTF_8
                    );

            sendProjectData(
                    player,
                    project,
                    file,
                    source
            );
        } catch (Exception exception) {
            sendMessage(
                    player,
                    "Project read failed: " +
                            exception.getMessage()
            );
        }
    }

    private static void sendProjectData(
            ServerPlayerEntity player,
            String project,
            String file
    ) {
        Path path =
                projectFile(
                        player,
                        project,
                        file
                );

        if (path == null ||
                !Files.isRegularFile(path)) {
            sendMessage(
                    player,
                    "Project file not found."
            );
            return;
        }

        try {
            String source =
                    Files.readString(
                            path,
                            StandardCharsets.UTF_8
                    );

            sendProjectData(
                    player,
                    project,
                    file,
                    source
            );
        } catch (Exception exception) {
            sendMessage(
                    player,
                    "Project read failed: " +
                            exception.getMessage()
            );
        }
    }

    private static void sendProjectData(
            ServerPlayerEntity player,
            String project,
            String file,
            String source
    ) {
        Path projectDirectory =
                ScriptProjects.projectDirectory(
                        player.getCommandSource().getServer(),
                        project
                );

        if (!Files.isDirectory(projectDirectory)) {
            sendMessage(
                    player,
                    "Project not found: " + project
            );
            return;
        }

        try {
            Path sourceDirectory =
                    projectDirectory.resolve("src");

            List<String> files;

            try (var stream = Files.walk(sourceDirectory)) {
                files = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .map(path ->
                                sourceDirectory
                                        .relativize(path)
                                        .toString()
                                        .replace('\\', '/')
                        )
                        .sorted()
                        .toList();
            }

            String fileIndex =
                    String.join("\n", files);

            ServerPlayNetworking.send(
                    player,
                    new ProjectDataPayload(
                            project,
                            file,
                            fileIndex,
                            source
                    )
            );
        } catch (Exception exception) {
            sendMessage(
                    player,
                    "Project listing failed: " +
                            exception.getMessage()
            );
        }
    }

    private static String saveProjectFile(
            ServerPlayerEntity player,
            String project,
            String file,
            String source
    ) {
        if (source.length() > GafiScriptBlockEntity.MAX_SOURCE_LENGTH) {
            return "Source is too large.";
        }

        Path projectDirectory =
                ScriptProjects.projectDirectory(
                        player.getCommandSource().getServer(),
                        project
                );

        if (!Files.isDirectory(projectDirectory)) {
            return "Project not found: " + project;
        }

        Path target =
                projectFile(
                        player,
                        project,
                        file
                );

        if (target == null) {
            return "Unsafe project file path.";
        }

        if (!file.endsWith(".java")) {
            return "Project files must end with .java.";
        }

        try {
            Files.createDirectories(target.getParent());
            Files.writeString(
                    target,
                    source,
                    StandardCharsets.UTF_8
            );
            return null;
        } catch (Exception exception) {
            return "Project save failed: " +
                    exception.getMessage();
        }
    }

    private static Path projectFile(
            ServerPlayerEntity player,
            String project,
            String file
    ) {
        Path root =
                ScriptProjects.projectDirectory(
                        player.getCommandSource().getServer(),
                        project
                )
                .resolve("src")
                .toAbsolutePath()
                .normalize();

        String safeFile =
                file == null ? "Main.java" : file;

        Path target =
                root.resolve(safeFile)
                        .normalize();

        if (!target.startsWith(root)) {
            return null;
        }

        return target;
    }

    private static boolean canEdit(
            ServerPlayerEntity player
    ) {
        return player.getCommandSource()
                .getPermissions()
                .hasPermission(
                        DefaultPermissions.GAMEMASTERS
                );
    }

    private static void sendMessage(
            ServerPlayerEntity player,
            String message
    ) {
        ServerPlayNetworking.send(
                player,
                new MessagePayload(message)
        );
    }

    private static boolean saveBlock(
            ServerPlayerEntity player,
            BlockPos pos,
            String name,
            String source
    ) {
        if (source.length() >
                GafiScriptBlockEntity.MAX_SOURCE_LENGTH) {
            sendMessage(
                    player,
                    "Source is too large."
            );
            return false;
        }

        if (!(player.getCommandSource()
                .getWorld()
                .getBlockEntity(pos)
                instanceof GafiScriptBlockEntity blockEntity)) {
            sendMessage(
                    player,
                    "No GafiScript block found."
            );
            return false;
        }

        try {
            blockEntity.setScriptName(name);
            blockEntity.setSource(source);
            blockEntity.markDirty();
            return true;
        } catch (Exception exception) {
            sendMessage(
                    player,
                    exception.getMessage() == null
                            ? exception.getClass().getSimpleName()
                            : exception.getMessage()
            );
            return false;
        }
    }
}
