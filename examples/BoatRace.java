import com.gafipro.gafiscript.api.GafiBossBar;
import com.gafipro.gafiscript.api.GafiEventHandle;
import com.gafipro.gafiscript.api.GafiPosition;

import static com.gafipro.gafiscript.api.Gafi.*;

public class BoatRace {
    private static final GafiPosition START_BUTTON =
            GafiPosition.of(-1461, 58, 590);

    private static final GafiPosition TRACK_MIN =
            GafiPosition.of(-1465, 65, 587);

    private static final GafiPosition TRACK_MAX =
            GafiPosition.of(-1455, 66, 587);

    private static final GafiPosition FINISH_MIN =
            GafiPosition.of(-1400, 64, 587);

    private static final GafiPosition FINISH_MAX =
            GafiPosition.of(-1390, 67, 587);

    private static final String TRACK_BLOCK =
            "minecraft:ice";

    private static final String LOCK_BLOCK =
            "minecraft:iron_bars";

    private static boolean running;
    private static long startTick;
    private static int raceNumber;

    private static GafiBossBar bossBar;
    private static GafiEventHandle buttonListener;

    public static void start() {
        reset();

        buttonListener =
                events().onBlockUse(event -> {
                    if (!sameBlock(
                            event.position(),
                            START_BUTTON
                    )) {
                        return;
                    }

                    event.cancel();

                    if (running) {
                        event.player()
                                .sendMessage(
                                        "§cA boat race is already running."
                                );
                        return;
                    }

                    beginRace();
                });

        repeatTicks(
                1,
                BoatRace::tick
        );

        broadcast(
                "§7BoatRace loaded. " +
                        "§ePress the configured start button."
        );
    }

    private static void beginRace() {
        running = false;
        raceNumber++;

        closeTrack();

        broadcast("§eBoat Race #" + raceNumber);
        broadcast("§fGet ready!");

        scheduler().delaySeconds(
                1,
                () -> broadcast("§e3")
        );

        scheduler().delaySeconds(
                2,
                () -> broadcast("§e2")
        );

        scheduler().delaySeconds(
                3,
                () -> broadcast("§e1")
        );

        scheduler().delaySeconds(
                4,
                () -> {
                    openTrack();

                    running = true;
                    startTick =
                            server()
                                    .getTicks();

                    bossBar =
                            bossBar(
                                    "Boat Race",
                                    net.minecraft.entity.boss.ServerBossBar.Color.BLUE,
                                    net.minecraft.entity.boss.ServerBossBar.Style.PROGRESS
                            )
                            .progress(0.0f)
                            .show();

                    for (var player : players()) {
                        bossBar.addPlayer(player);
                    }

                    broadcast("§aGO!");
                }
        );
    }

    private static void tick() {
        if (!running) {
            return;
        }

        long elapsed =
                server().getTicks() -
                        startTick;

        float progress =
                Math.min(
                        1.0f,
                        elapsed / 1200.0f
                );

        if (bossBar != null) {
            bossBar.progress(progress);

            for (var player : players()) {
                bossBar.addPlayer(player);
            }
        }

        for (var player : players()) {
            if (inside(
                    player.position(),
                    FINISH_MIN,
                    FINISH_MAX
            )) {
                finish(player);
                return;
            }
        }

        if (elapsed >= 1200) {
            endWithoutWinner();
        }
    }

    private static void finish(
            com.gafipro.gafiscript.api.GafiPlayer winner
    ) {
        if (!running) {
            return;
        }

        running = false;

        long elapsed =
                server().getTicks() -
                        startTick;

        int seconds =
                (int) Math.ceil(
                        elapsed / 20.0
                );

        var scoreboard =
                scoreboard();

        var objective =
                scoreboard.objective(
                        "gafi_boat_wins",
                        "Boat Wins"
                );

        objective.addScore(
                winner.name(),
                1
        );

        if (bossBar != null) {
            bossBar.name(
                    "Winner: " +
                            winner.name()
            );
            bossBar.progress(1.0f);
        }

        broadcast(
                "§6Winner: §f" +
                        winner.name() +
                        " §7in §e" +
                        seconds +
                        "s"
        );

        scheduler().delaySeconds(
                5,
                BoatRace::reset
        );
    }

    private static void endWithoutWinner() {
        running = false;

        if (bossBar != null) {
            bossBar.hide();
        }

        broadcast(
                "§cBoat race ended without a winner."
        );

        scheduler().delaySeconds(
                3,
                BoatRace::reset
        );
    }

    private static void openTrack() {
        world().fill(
                TRACK_MIN,
                TRACK_MAX,
                TRACK_BLOCK
        );
    }

    private static void closeTrack() {
        world().fill(
                TRACK_MIN,
                TRACK_MAX,
                LOCK_BLOCK
        );
    }

    private static void reset() {
        running = false;

        closeTrack();

        if (bossBar != null) {
            bossBar.hide();
            bossBar = null;
        }
    }

    private static boolean inside(
            GafiPosition point,
            GafiPosition min,
            GafiPosition max
    ) {
        return point.x() >= Math.min(min.x(), max.x()) &&
                point.x() <= Math.max(min.x(), max.x()) &&
                point.y() >= Math.min(min.y(), max.y()) &&
                point.y() <= Math.max(min.y(), max.y()) &&
                point.z() >= Math.min(min.z(), max.z()) &&
                point.z() <= Math.max(min.z(), max.z());
    }

    private static boolean sameBlock(
            GafiPosition a,
            GafiPosition b
    ) {
        return Math.floor(a.x()) == Math.floor(b.x()) &&
                Math.floor(a.y()) == Math.floor(b.y()) &&
                Math.floor(a.z()) == Math.floor(b.z());
    }
}
