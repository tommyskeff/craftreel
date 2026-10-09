package dev.tommyjs.craftreel.protocol.chunk;

import org.jetbrains.annotations.Nullable;

public record ChunkSectionBlockEntityDelta(int index, byte @Nullable [] before, byte @Nullable [] after) {
}
