package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveMeta;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScores;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardMeta;
import dev.tommyjs.reel.recorder.EntityRecorder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class ScoreboardRecorder {

    private static final Map<MinecraftRecording, ScoreboardRecorder> DEFAULTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final MinecraftRecording recording;
    private final Identifier contextId;

    private ScoreboardRecorder(@NotNull MinecraftRecording recording, @NotNull Identifier contextId) {
        this.recording = recording;
        this.contextId = contextId;
    }

    public static @NotNull ScoreboardRecorder attach(@NotNull MinecraftRecording recording, @NotNull Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.SCOREBOARD);
        recorder.recordState(CraftReelProtocol.Tracks.SCOREBOARD_META, new ScoreboardMeta(identifier));
        return new ScoreboardRecorder(recording, identifier);
    }

    public static @NotNull ScoreboardRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.SCOREBOARD));
    }

    public @NotNull ObjectiveHandle createObjective(@NotNull String name) {
        EntityRecorder child = recording.getRecorder().createEntity(CraftReelProtocol.Entities.OBJECTIVE);
        child.recordState(CraftReelProtocol.Tracks.OBJECTIVE_META, new ObjectiveMeta(contextId, name));
        child.recordState(CraftReelProtocol.Tracks.OBJECTIVE_SCORES, ObjectiveScores.empty());
        return new ObjectiveHandle(child);
    }

}
