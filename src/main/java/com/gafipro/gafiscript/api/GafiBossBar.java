package com.gafipro.gafiscript.api;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class GafiBossBar {
    private final ServerBossBar bar;

    public GafiBossBar(
            String name,
            ServerBossBar.Color color,
            ServerBossBar.Style style
    ) {
        this.bar = new ServerBossBar(
                Text.literal(name),
                color,
                style
        );
    }

    public GafiBossBar progress(float progress) {
        bar.setPercent(
                Math.max(
                        0.0f,
                        Math.min(progress, 1.0f)
                )
        );
        return this;
    }

    public GafiBossBar name(String name) {
        bar.setName(Text.literal(name));
        return this;
    }

    public GafiBossBar addPlayer(GafiPlayer player) {
        bar.addPlayer(player.raw());
        return this;
    }

    public GafiBossBar addPlayer(ServerPlayerEntity player) {
        bar.addPlayer(player);
        return this;
    }

    public GafiBossBar removePlayer(GafiPlayer player) {
        bar.removePlayer(player.raw());
        return this;
    }

    public GafiBossBar show() {
        bar.setVisible(true);
        return this;
    }

    public GafiBossBar hide() {
        bar.setVisible(false);
        return this;
    }

    public ServerBossBar raw() {
        return bar;
    }
}
