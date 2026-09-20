package com.gafipro.gafiscript.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class GafiWorld {
    private final MinecraftServer server;
    private final ServerWorld world;

    public GafiWorld(MinecraftServer server) {
        this(server, server.getOverworld());
    }

    public GafiWorld(MinecraftServer server, ServerWorld world) {
        this.server = server;
        this.world = world;
    }

    public ServerWorld raw() {
        return world;
    }

    public String dimension() {
        return world.getRegistryKey().getValue().toString();
    }

    public long time() {
        return world.getTimeOfDay();
    }

    public void setTime(long time) {
        server.execute(() -> world.setTimeOfDay(time));
    }

    public boolean isDay() {
        return world.isDay();
    }

    public boolean isNight() {
        return world.isNight();
    }

    public GafiEntities entities() {
        return new GafiEntities(world);
    }

    public boolean isAir(GafiPosition position) {
        return world.getBlockState(
                toBlockPos(position)
        ).isAir();
    }

    public String block(GafiPosition position) {
        return Registries.BLOCK.getId(
                world.getBlockState(
                        toBlockPos(position)
                ).getBlock()
        ).toString();
    }

    public void setBlock(
            GafiPosition position,
            String blockId
    ) {
        server.execute(() -> {
            Block block =
                    Registries.BLOCK.get(
                            Identifier.of(blockId)
                    );

            if (block == null) return;

            world.setBlockState(
                    toBlockPos(position),
                    block.getDefaultState()
            );
        });
    }

    public void breakBlock(
            GafiPosition position,
            boolean drop
    ) {
        server.execute(() ->
                world.breakBlock(
                        toBlockPos(position),
                        drop
                )
        );
    }

    public GafiEntity spawnEntity(
            String entityId,
            GafiPosition position
    ) {
        var type = Registries.ENTITY_TYPE.get(
                Identifier.of(entityId)
        );

        Entity entity = type.create(
                world,
                SpawnReason.COMMAND
        );

        if (entity == null) {
            return null;
        }

        entity.refreshPositionAndAngles(
                position.x(),
                position.y(),
                position.z(),
                entity.getYaw(),
                entity.getPitch()
        );

        server.execute(() ->
                world.spawnEntity(entity)
        );

        return new GafiEntity(entity);
    }

    public void spawnParticles(
            ParticleEffect effect,
            GafiPosition position,
            int count,
            double spreadX,
            double spreadY,
            double spreadZ,
            double speed
    ) {
        server.execute(() ->
                world.spawnParticles(
                        effect,
                        position.x(),
                        position.y(),
                        position.z(),
                        Math.max(0, count),
                        spreadX,
                        spreadY,
                        spreadZ,
                        speed
                )
        );
    }

    public void playSound(
            String soundId,
            GafiPosition position,
            float volume,
            float pitch
    ) {
        SoundEvent sound =
                Registries.SOUND_EVENT.get(
                        Identifier.of(soundId)
                );

        if (sound == null) return;

        playSound(
                sound,
                position,
                volume,
                pitch
        );
    }

    public void playSound(
            SoundEvent sound,
            GafiPosition position,
            float volume,
            float pitch
    ) {
        server.execute(() ->
                world.playSound(
                        null,
                        toBlockPos(position),
                        sound,
                        SoundCategory.MASTER,
                        volume,
                        pitch
                )
        );
    }

    public int height() {
        return world.getHeight();
    }

    public int bottomY() {
        return world.getBottomY();
    }

    public GafiPosition spawnPoint() {
        BlockPos pos =
                world.getSpawnPos();

        return new GafiPosition(
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    public void setSpawnPoint(
            GafiPosition position
    ) {
        server.execute(() ->
                world.setSpawnPos(
                        toBlockPos(position),
                        0.0f
                )
        );
    }

    public String biome(
            GafiPosition position
    ) {
        return world.getBiome(
                        toBlockPos(position)
                )
                .getKey()
                .map(
                        key -> key.getValue().toString()
                )
                .orElse("minecraft:unknown");
    }

    public void clearWeather() {
        server.execute(() ->
                world.setWeather(
                        0,
                        0,
                        false,
                        false
                )
        );
    }

    public void rain(
            int durationTicks
    ) {
        server.execute(() ->
                world.setWeather(
                        0,
                        Math.max(0, durationTicks),
                        true,
                        false
                )
        );
    }

    public void thunder(
            int durationTicks
    ) {
        server.execute(() ->
                world.setWeather(
                        0,
                        Math.max(0, durationTicks),
                        true,
                        true
                )
        );
    }

    public boolean gameRule(
            String name
    ) {
        return world.getGameRules()
                .getBoolean(
                        net.minecraft.world.GameRules.get(
                                name
                        )
                );
    }

    public void setGameRule(
            String name,
            boolean value
    ) {
        server.execute(() ->
                world.getGameRules()
                        .get(
                                net.minecraft.world.GameRules.get(name)
                        )
                        .set(
                                value,
                                server
                        )
        );
    }

    public double worldBorderSize() {
        return world.getWorldBorder().getSize();
    }

    public void setWorldBorderSize(
            double size
    ) {
        server.execute(() ->
                world.getWorldBorder()
                        .setSize(
                                Math.max(1.0, size)
                        )
        );
    }

    public GafiWorld dimension(
            String dimensionId
    ) {
        var key =
                net.minecraft.registry.RegistryKey.of(
                        net.minecraft.registry.RegistryKeys.WORLD,
                        Identifier.of(dimensionId)
                );

        ServerWorld target =
                server.getWorld(key);

        if (target == null) {
            throw new IllegalArgumentException(
                    "Dimension is not loaded: " +
                            dimensionId
            );
        }

        return new GafiWorld(
                server,
                target
        );
    }

    public void fill(
            GafiPosition from,
            GafiPosition to,
            String blockId
    ) {
        Block block =
                Registries.BLOCK.get(
                        Identifier.of(blockId)
                );

        if (block == null) return;

        BlockState state =
                block.getDefaultState();

        BlockPos a =
                toBlockPos(from);
        BlockPos b =
                toBlockPos(to);

        int minX =
                Math.min(
                        a.getX(),
                        b.getX()
                );

        int maxX =
                Math.max(
                        a.getX(),
                        b.getX()
                );

        int minY =
                Math.min(
                        a.getY(),
                        b.getY()
                );

        int maxY =
                Math.max(
                        a.getY(),
                        b.getY()
                );

        int minZ =
                Math.min(
                        a.getZ(),
                        b.getZ()
                );

        int maxZ =
                Math.max(
                        a.getZ(),
                        b.getZ()
                );

        server.execute(() -> {
            long volume =
                    (long) (maxX - minX + 1) *
                    (maxY - minY + 1) *
                    (maxZ - minZ + 1);

            if (volume > 250_000L) {
                throw new IllegalArgumentException(
                        "Region is too large: " +
                                volume +
                                " blocks."
                );
            }

            for (int x = minX;
                 x <= maxX;
                 x++) {

                for (int y = minY;
                     y <= maxY;
                     y++) {

                    for (int z = minZ;
                         z <= maxZ;
                         z++) {

                        world.setBlockState(
                                new BlockPos(
                                        x,
                                        y,
                                        z
                                ),
                                state
                        );
                    }
                }
            }
        });
    }

    public List<GafiPosition> findBlocks(
            GafiPosition from,
            GafiPosition to,
            String blockId,
            int limit
    ) {
        Block block =
                Registries.BLOCK.get(
                        Identifier.of(blockId)
                );

        if (block == null) return List.of();

        BlockPos a =
                toBlockPos(from);
        BlockPos b =
                toBlockPos(to);

        int minX =
                Math.min(a.getX(), b.getX());

        int maxX =
                Math.max(a.getX(), b.getX());

        int minY =
                Math.min(a.getY(), b.getY());

        int maxY =
                Math.max(a.getY(), b.getY());

        int minZ =
                Math.min(a.getZ(), b.getZ());

        int maxZ =
                Math.max(a.getZ(), b.getZ());

        List<GafiPosition> result =
                new ArrayList<>();

        outer:
        for (int x = minX;
             x <= maxX;
             x++) {

            for (int y = minY;
                 y <= maxY;
                 y++) {

                for (int z = minZ;
                     z <= maxZ;
                     z++) {

                    if (world.getBlockState(
                            new BlockPos(x, y, z)
                    ).isOf(block)) {

                        result.add(
                                new GafiPosition(
                                        x,
                                        y,
                                        z
                                )
                        );

                        if (result.size() >=
                                Math.max(
                                        1,
                                        limit
                                )) {
                            break outer;
                        }
                    }
                }
            }
        }

        return result;
    }

    private static BlockPos toBlockPos(
            GafiPosition position
    ) {
        return BlockPos.ofFloored(
                position.x(),
                position.y(),
                position.z()
        );
    }
}
