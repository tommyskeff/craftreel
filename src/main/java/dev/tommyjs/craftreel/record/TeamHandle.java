package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol.Tracks;
import dev.tommyjs.craftreel.protocol.team.TeamInfo;
import dev.tommyjs.craftreel.protocol.team.TeamMembersDelta;
import dev.tommyjs.reel.recorder.EntityRecorder;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public final class TeamHandle {

    private final EntityRecorder recorder;
    private final Set<String> members = new LinkedHashSet<>();

    TeamHandle(@NotNull EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public void setInfo(@NotNull TeamInfo info) {
        recorder.recordState(Tracks.TEAM_INFO, info);
    }

    public void addMembers(@NotNull Set<String> entries) {
        Set<String> added = new LinkedHashSet<>(entries);
        added.removeAll(members);
        if (!added.isEmpty()) {
            recorder.recordDelta(Tracks.TEAM_MEMBERS, new TeamMembersDelta.AddEntries(added));
            members.addAll(added);
        }
    }

    public void removeMembers(@NotNull Set<String> entries) {
        Set<String> removed = new LinkedHashSet<>(entries);
        removed.retainAll(members);
        if (!removed.isEmpty()) {
            recorder.recordDelta(Tracks.TEAM_MEMBERS, new TeamMembersDelta.RemoveEntries(removed));
            members.removeAll(removed);
        }
    }

    public void remove() {
        recorder.close();
    }

}
