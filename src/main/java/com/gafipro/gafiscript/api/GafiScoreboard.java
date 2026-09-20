package com.gafipro.gafiscript.api;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreHolder;

public final class GafiScoreboard {
    private final MinecraftServer server;
    private final Scoreboard scoreboard;

    GafiScoreboard(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
    }

    public GafiObjective objective(
            String name,
            String displayName
    ) {
        return createObjective(name, displayName);
    }

    public GafiObjective createObjective(
            String name,
            String displayName
    ) {
        ScoreboardObjective existing =
                scoreboard.getNullableObjective(name);

        if (existing != null) {
            return new GafiObjective(existing, scoreboard);
        }

        ScoreboardObjective objective =
                scoreboard.addObjective(
                        name,
                        ScoreboardCriterion.DUMMY,
                        Text.literal(displayName),
                        ScoreboardCriterion.RenderType.INTEGER,
                        true,
                        null
                );

        return new GafiObjective(objective, scoreboard);
    }

    public GafiObjective get(String name) {
        ScoreboardObjective objective =
                scoreboard.getNullableObjective(name);

        return objective == null
                ? null
                : new GafiObjective(objective, scoreboard);
    }

    public void remove(String name) {
        ScoreboardObjective objective =
                scoreboard.getNullableObjective(name);

        if (objective != null) {
            server.execute(() ->
                    scoreboard.removeObjective(objective)
            );
        }
    }

    public void displaySidebar(GafiObjective objective) {
        server.execute(() ->
                scoreboard.setObjectiveSlot(
                        ScoreboardDisplaySlot.SIDEBAR,
                        objective.raw()
                )
        );
    }

    public void displayBelowName(GafiObjective objective) {
        server.execute(() ->
                scoreboard.setObjectiveSlot(
                        ScoreboardDisplaySlot.BELOW_NAME,
                        objective.raw()
                )
        );
    }

    public void displayList(GafiObjective objective) {
        server.execute(() ->
                scoreboard.setObjectiveSlot(
                        ScoreboardDisplaySlot.LIST,
                        objective.raw()
                )
        );
    }

    public Scoreboard raw() {
        return scoreboard;
    }

    public static final class GafiObjective {
        private final ScoreboardObjective objective;
        private final Scoreboard scoreboard;

        private GafiObjective(
                ScoreboardObjective objective,
                Scoreboard scoreboard
        ) {
            this.objective = objective;
            this.scoreboard = scoreboard;
        }

        public String name() {
            return objective.getName();
        }

        public String displayName() {
            return objective.getDisplayName().getString();
        }

        public GafiObjective displayName(String value) {
            objective.setDisplayName(
                    Text.literal(value)
            );
            return this;
        }

        public int score(String holder) {
            ScoreHolder scoreHolder =
                    ScoreHolder.fromName(holder);

            var score =
                    scoreboard.getScore(
                            scoreHolder,
                            objective
                    );

            return score == null
                    ? 0
                    : score.getScore();
        }

        public void setScore(
                String holder,
                int value
        ) {
            ScoreHolder scoreHolder =
                    ScoreHolder.fromName(holder);

            serverExecute(() ->
                    scoreboard.getOrCreateScore(
                            scoreHolder,
                            objective
                    ).setScore(value)
            );
        }

        public void addScore(
                String holder,
                int amount
        ) {
            setScore(
                    holder,
                    score(holder) + amount
            );
        }

        public void removeScore(
                String holder
        ) {
            serverExecute(() ->
                    scoreboard.removeScore(
                            ScoreHolder.fromName(holder),
                            objective
                    )
            );
        }

        private void serverExecute(Runnable action) {
            // The underlying scoreboard belongs to the server.
            // Callers may use these helpers from script event callbacks.
            action.run();
        }

        public ScoreboardObjective raw() {
            return objective;
        }
    }
}
