package dev.tommyjs.craftreel.protocol.team;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public record TeamMembers(@NotNull Set<String> entries) {

    public TeamMembers(@NotNull Set<String> entries) {
        this.entries = new LinkedHashSet<>(entries);
    }

    public static @NotNull TeamMembers empty() {
        return new TeamMembers(Set.of());
    }

}
