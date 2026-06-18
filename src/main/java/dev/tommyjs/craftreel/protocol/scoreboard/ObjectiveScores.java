package dev.tommyjs.craftreel.protocol.scoreboard;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public record ObjectiveScores(@NotNull Map<String, Integer> scores) {

    public ObjectiveScores(@NotNull Map<String, Integer> scores) {
        this.scores = new LinkedHashMap<>(scores);
    }

    public static @NotNull ObjectiveScores empty() {
        return new ObjectiveScores(Map.of());
    }

}
