package com.gafipro.gafiscript.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
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

    public boolean isAir(GafiPosition position) {
        return world.getBlockState(toBlockPos(position)).isAir();
    }

    public String block(GafiPosition position) {
        return Registries.BLOCK.getId(
                world.getBlockState(toBlockPos(position)).getBlock()
        ).toString();
    }

    public void setBlock(GafiPosition position, String blockId) {
        server.execute(() -> {
            Block block = Registries.BLOCK.get(Identifier.of(blockId));
            if (block == null) return;
            world.setBlockState(toBlockPos(position), block.getDefaultState());
        });
    }

    public void breakBlock(GafiPosition position, boolean drop) {
        server.execute(() -> world.breakBlock(toBlockPos(position), drop));
    }

    public void fill(GafiPosition from, GafiPosition to, String blockId) {
        Block block = Registries.BLOCK.get(Identifier.of(blockId));
        if (block == null) return;
        BlockState state = block.getDefaultState();

        BlockPos a = toBlockPos(from);
        BlockPos b = toBlockPos(to);

        int minX = Math.min(a.getX(), b.getX());
        int maxX = Math.max(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int maxY = Math.max(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxZ = Math.max(a.getZ(), b.getZ());

        server.execute(() -> {
            long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            if (volume > 250_000L) {
                throw new IllegalArgumentException("Region is too large: " + volume + " blocks.");
            }

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        world.setBlockState(new BlockPos(x, y, z), state);
                    }
                }
            }
        });
    }

    public List<GafiPosition> findBlocks(GafiPosition from, GafiPosition to, String blockId, int limit) {
        Block block = Registries.BLOCK.get(Identifier.of(blockId));
        if (block == null) return List.of();

        BlockPos a = toBlockPos(from);
        BlockPos b = toBlockPos(to);
        int minX = Math.min(a.getX(), b.getX());
        int maxX = Math.max(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int maxY = Math.max(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxZ = Math.max(a.getZ(), b.getZ());

        List<GafiPosition> result = new ArrayList<>();
        outer:
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (world.getBlockState(new BlockPos(x, y, z)).isOf(block)) {
                        result.add(new GafiPosition(x, y, z));
                        if (result.size() >= Math.max(1, limit)) break outer;
                    }
                }
            }
        }

        return result;
    }

    private static BlockPos toBlockPos(GafiPosition position) {
        return BlockPos.ofFloored(position.x(), position.y(), position.z());
    }
}
