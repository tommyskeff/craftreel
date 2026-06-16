package dev.tommyjs.craftreel.protocol.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record EntityMeta(@NotNull UUID id, @NotNull Identifier worldId, @NotNull EntityType type, int objectData) {
}
