import static com.gafipro.gafiscript.api.Gafi.*;

public class RewardShowcase {
    public static void start() {
        events().onPlayerJoin(player ->
                player.sendMessage(
                        "§bRewardShowcase loaded."
                )
        );

        var sword =
                items()
                        .item("minecraft:diamond_sword")
                        .name("§bGafi Blade")
                        .lore(
                                "§7Created with Data Components",
                                "§7from the GafiScript ItemBuilder."
                        )
                        .customModelData(1001)
                        .glint(true)
                        .build();

        var gui =
                gui()
                        .create(
                                "Reward Showcase",
                                3
                        );

        gui.setItem(
                11,
                sword
        );

        gui.setItem(
                13,
                items()
                        .item("minecraft:emerald")
                        .name("§aInformation")
                        .lore(
                                "§7This is a GUI example.",
                                "§7It does not implement gambling."
                        )
                        .build()
        );

        gui.setItem(
                15,
                items()
                        .item("minecraft:clock")
                        .name("§eScheduler demo")
                        .build()
        );

        gui.onClick(event -> {
            if (event.slot() == 15 &&
                    event.player() != null) {

                event.cancel();

                var player =
                        event.player();

                player.sendMessage(
                        "§eScheduling a message in 3 seconds..."
                );

                delaySeconds(
                        3,
                        () -> player.sendMessage(
                                "§aThree seconds passed."
                        )
                );
            }
        });

        delaySeconds(
                1,
                () -> {
                    for (var player : players()) {
                        gui.open(player);
                    }
                }
        );
    }
}
