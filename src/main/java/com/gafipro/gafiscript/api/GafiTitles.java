package com.gafipro.gafiscript.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;

public final class GafiTitles {
    private GafiTitles() {}

    public static void title(
            GafiPlayer player,
            String title
    ) {
        ServerPlayerEntity raw = player.raw();
        raw.networkHandler.sendPacket(
                new TitleS2CPacket(Text.literal(title))
        );
    }

    public static void subtitle(
            GafiPlayer player,
            String subtitle
    ) {
        ServerPlayerEntity raw = player.raw();
        raw.networkHandler.sendPacket(
                new SubtitleS2CPacket(Text.literal(subtitle))
        );
    }
}
