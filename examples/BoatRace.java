import static com.gafipro.gafiscript.api.Gafi.*;

public class BoatRace {
    public static void start() {
        broadcast("§e3");

        scheduler().delaySeconds(1, () -> {
            broadcast("§e2");

            scheduler().delaySeconds(1, () -> {
                broadcast("§e1");

                scheduler().delaySeconds(1, () -> {
                    broadcast("§aGO!");

                    world().fill(
                        GafiPosition.of(-1465, 65, 587),
                        GafiPosition.of(-1455, 66, 587),
                        "minecraft:air"
                    );

                    scheduler().delaySeconds(2, () -> {
                        world().setBlock(
                            GafiPosition.of(-1461, 58, 590),
                            "minecraft:redstone_block"
                        );
                    });
                });
            });
        });
    }
}
