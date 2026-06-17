package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.team.TeamMembers;
import dev.tommyjs.craftreel.protocol.team.TeamMeta;
import dev.tommyjs.craftreel.protocol.team.TeamsMeta;
import dev.tommyjs.reel.recorder.EntityRecorder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class TeamRecorder {

    private static final Map<MinecraftRecording, TeamRecorder> DEFAULTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final MinecraftRecording recording;
    private final Identifier contextId;

    private TeamRecorder(@NotNull MinecraftRecording recording, @NotNull Identifier contextId) {
        this.recording = recording;
        this.contextId = contextId;
    }

    public static @NotNull TeamRecorder attach(@NotNull MinecraftRecording recording, @NotNull Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TEAMS);
        recorder.recordState(CraftReelProtocol.Tracks.TEAMS_META, new TeamsMeta(identifier));
        return new TeamRecorder(recording, identifier);
    }

    public static @NotNull TeamRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.TEAM));
    }

    public @NotNull TeamHandle createTeam(@NotNull String name) {
        EntityRecorder child = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TEAM);
        child.recordState(CraftReelProtocol.Tracks.TEAM_META, new TeamMeta(contextId, name));
        child.recordState(CraftReelProtocol.Tracks.TEAM_MEMBERS, TeamMembers.empty());
        return new TeamHandle(child);
    }

}
