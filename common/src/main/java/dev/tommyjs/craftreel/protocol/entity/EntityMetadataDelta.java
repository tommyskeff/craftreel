package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.NotNull;

public record EntityMetadataDelta(int index, @NotNull EntityMetadataValue before, @NotNull EntityMetadataValue after) {
}
