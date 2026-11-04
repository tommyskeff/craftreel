package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record EntityMeta(@NotNull UUID id, @NotNull Identifier worldId, @NotNull String type, int objectData) {
}
