package dev.tommyjs.craftreel.protocol.scoreboard;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjectiveScoresModel implements TrackModel<ObjectiveScores, ObjectiveScoreDelta> {

    @Override
    public @NotNull ObjectiveScores applyDelta(@NotNull ObjectiveScores state, @NotNull ObjectiveScoreDelta delta) {
        if (delta instanceof ObjectiveScoreDelta.AddScore add) {
            state.scores().put(add.entry(), add.value());
        } else if (delta instanceof ObjectiveScoreDelta.UpdateScore update) {
            state.scores().put(update.entry(), update.after());
        } else if (delta instanceof ObjectiveScoreDelta.RemoveScore remove) {
            state.scores().remove(remove.entry());
        } else {
            throw new IllegalStateException("Unknown score delta: " + delta);
        }
        return state;
    }

    @Override
    public @NotNull ObjectiveScores cloneState(@NotNull ObjectiveScores state) {
        return new ObjectiveScores(state.scores());
    }

    @Override
    public @NotNull List<ObjectiveScoreDelta> condense(@NotNull ObjectiveScores ref, @NotNull List<ObjectiveScoreDelta> deltas) {
        return List.copyOf(deltas);
    }

    @Override
    public @NotNull ObjectiveScoreDelta reverse(@NotNull ObjectiveScoreDelta delta) {
        if (delta instanceof ObjectiveScoreDelta.AddScore add) {
            return new ObjectiveScoreDelta.RemoveScore(add.entry(), add.value());
        } else if (delta instanceof ObjectiveScoreDelta.RemoveScore remove) {
            return new ObjectiveScoreDelta.AddScore(remove.entry(), remove.before());
        } else if (delta instanceof ObjectiveScoreDelta.UpdateScore update) {
            return new ObjectiveScoreDelta.UpdateScore(update.entry(), update.after(), update.before());
        } else {
            throw new IllegalStateException("Unknown score delta: " + delta);
        }
    }

}
