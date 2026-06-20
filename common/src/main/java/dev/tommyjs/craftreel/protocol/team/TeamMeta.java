package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record TeamMeta(@NotNull Identifier contextId, @NotNull String name) {
}
