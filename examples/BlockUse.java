import static com.gafipro.gafiscript.api.Gafi.*;

public class BlockUse {
    public static void start() {
        events().onBlockUse(event -> {
            event.player().sendMessage(
                "Usaste um bloco em " + event.position()
            );
        });
    }
}
