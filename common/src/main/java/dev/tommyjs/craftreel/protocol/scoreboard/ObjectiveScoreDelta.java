package dev.tommyjs.craftreel.protocol.scoreboard;

import org.jetbrains.annotations.NotNull;

public sealed interface ObjectiveScoreDelta
    permits ObjectiveScoreDelta.AddScore, ObjectiveScoreDelta.UpdateScore, ObjectiveScoreDelta.RemoveScore {

    record AddScore(@NotNull String entry, int value) implements ObjectiveScoreDelta {
    }

    record UpdateScore(@NotNull String entry, int before, int after) implements ObjectiveScoreDelta {
    }

    record RemoveScore(@NotNull String entry, int before) implements ObjectiveScoreDelta {
    }

}
