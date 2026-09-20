package com.gafipro.gafiscript.net;

import com.gafipro.gafiscript.GafiScriptMod;
import com.gafipro.gafiscript.block.GafiScriptBlockEntity;
import com.gafipro.gafiscript.runtime.ScriptManager;
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

public final class GafiScriptNetworking {
    private static final Identifier REQUEST_ID = GafiScriptMod.id("request_script");
    private static final Identifier SAVE_ID = GafiScriptMod.id("save_script");
    private static final Identifier RUN_ID = GafiScriptMod.id("run_script");
    private static final Identifier SCRIPT_DATA_ID = GafiScriptMod.id("script_data");
    private static final Identifier MESSAGE_ID = GafiScriptMod.id("message");

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

    private GafiScriptNetworking() {}

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(RequestScriptPayload.ID, RequestScriptPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveScriptPayload.ID, SaveScriptPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RunScriptPayload.ID, RunScriptPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScriptDataPayload.ID, ScriptDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MessagePayload.ID, MessagePayload.CODEC);

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

            ScriptManager.runSourceAsync(player.getCommandSource().getServer(), payload.name(), payload.source())
                    .whenComplete((result, throwable) -> player.getCommandSource().getServer().execute(() -> {
                        String message = throwable == null
                                ? result
                                : "Runtime failure: " + throwable.getMessage();
                        ServerPlayNetworking.send(player, new MessagePayload(message));
                    }));
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

    private static boolean canEdit(ServerPlayerEntity player) {
        return player.getCommandSource()
                .getPermissions()
                .hasPermission(DefaultPermissions.GAMEMASTERS);
    }

    private static boolean saveBlock(ServerPlayerEntity player, BlockPos pos, String name, String source) {
        if (source.length() > GafiScriptBlockEntity.MAX_SOURCE_LENGTH) {
            ServerPlayNetworking.send(player, new MessagePayload("Source is too large."));
            return false;
        }
        if (!(player.getCommandSource().getWorld().getBlockEntity(pos) instanceof GafiScriptBlockEntity blockEntity)) {
            ServerPlayNetworking.send(player, new MessagePayload("No GafiScript block found."));
            return false;
        }

        try {
            blockEntity.setScriptName(name);
            blockEntity.setSource(source);
            blockEntity.markDirty();
            return true;
        } catch (Exception e) {
            ServerPlayNetworking.send(player, new MessagePayload(
                    e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()
            ));
            return false;
        }
    }
}
