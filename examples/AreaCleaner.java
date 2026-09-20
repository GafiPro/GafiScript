import static com.gafipro.gafiscript.api.Gafi.*;

public class AreaCleaner {
    public static void start() {
        world().fill(
            GafiPosition.of(-10, 64, -10),
            GafiPosition.of(10, 70, 10),
            "minecraft:air"
        );
    }
}
