package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol.Tracks;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveInfo;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScoreDelta;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardRenderType;
import dev.tommyjs.reel.recorder.EntityRecorder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ObjectiveHandle {

    private final EntityRecorder recorder;
    private final Map<String, Integer> scores = new LinkedHashMap<>();

    ObjectiveHandle(@NotNull EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public void setInfo(@NotNull Component displayName, @NotNull ScoreboardRenderType renderType, @Nullable Integer slot) {
        recorder.recordState(Tracks.OBJECTIVE_INFO, new ObjectiveInfo(displayName, renderType, slot));
    }

    public void setScore(@NotNull String entry, int value) {
        Integer prev = scores.get(entry);
        if (prev == null) {
            recorder.recordDelta(Tracks.OBJECTIVE_SCORES, new ObjectiveScoreDelta.AddScore(entry, value));
        } else if (prev != value) {
            recorder.recordDelta(Tracks.OBJECTIVE_SCORES, new ObjectiveScoreDelta.UpdateScore(entry, prev, value));
        } else {
            return;
        }
        scores.put(entry, value);
    }

    public void removeScore(@NotNull String entry) {
        Integer prev = scores.remove(entry);
        if (prev == null) {
            return;
        }
        recorder.recordDelta(Tracks.OBJECTIVE_SCORES, new ObjectiveScoreDelta.RemoveScore(entry, prev));
    }

    public void remove() {
        recorder.close();
    }

}
