import static com.gafipro.gafiscript.api.Gafi.*;

public class Countdown {
    public static void start() {
        broadcast("§e3");

        scheduler().delaySeconds(1, () -> {
            broadcast("§e2");

            scheduler().delaySeconds(1, () -> {
                broadcast("§e1");

                scheduler().delaySeconds(1, () -> {
                    broadcast("§aGO!");
                });
            });
        });
    }
}
