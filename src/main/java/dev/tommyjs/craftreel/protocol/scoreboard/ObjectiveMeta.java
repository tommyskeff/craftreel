package dev.tommyjs.craftreel.protocol.scoreboard;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record ObjectiveMeta(@NotNull Identifier contextId, @NotNull String name) {
}
