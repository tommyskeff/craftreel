package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record EnvironmentState(@NotNull Identifier worldId, long time, boolean storm, boolean thundering) {
}
