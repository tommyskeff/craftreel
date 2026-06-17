package dev.tommyjs.craftreel.protocol.team;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public sealed interface TeamMembersDelta
    permits TeamMembersDelta.AddEntries, TeamMembersDelta.RemoveEntries {

    record AddEntries(@NotNull Set<String> entries) implements TeamMembersDelta {
    }

    record RemoveEntries(@NotNull Set<String> entries) implements TeamMembersDelta {
    }

}
