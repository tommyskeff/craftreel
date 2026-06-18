package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamMembersModel implements TrackModel<TeamMembers, TeamMembersDelta> {

    @Override
    public @NotNull TeamMembers applyDelta(@NotNull TeamMembers state, @NotNull TeamMembersDelta delta) {
        if (delta instanceof TeamMembersDelta.AddEntries add) {
            state.entries().addAll(add.entries());
        } else if (delta instanceof TeamMembersDelta.RemoveEntries remove) {
            state.entries().removeAll(remove.entries());
        } else {
            throw new IllegalStateException("Unknown team members delta: " + delta);
        }
        return state;
    }

    @Override
    public @NotNull TeamMembers cloneState(@NotNull TeamMembers state) {
        return new TeamMembers(state.entries());
    }

    @Override
    public @NotNull List<TeamMembersDelta> condense(@NotNull TeamMembers ref, @NotNull List<TeamMembersDelta> deltas) {
        return List.copyOf(deltas);
    }

    @Override
    public @NotNull TeamMembersDelta reverse(@NotNull TeamMembersDelta delta) {
        if (delta instanceof TeamMembersDelta.AddEntries add) {
            return new TeamMembersDelta.RemoveEntries(add.entries());
        } else if (delta instanceof TeamMembersDelta.RemoveEntries remove) {
            return new TeamMembersDelta.AddEntries(remove.entries());
        } else {
            throw new IllegalStateException("Unknown team members delta: " + delta);
        }
    }

}
