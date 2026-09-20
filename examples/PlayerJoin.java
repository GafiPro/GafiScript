import static com.gafipro.gafiscript.api.Gafi.*;

public class PlayerJoin {
    public static void start() {
        events().onPlayerJoin(player -> {
            player.sendMessage("§aBem-vindo ao servidor!");
        });
    }
}
