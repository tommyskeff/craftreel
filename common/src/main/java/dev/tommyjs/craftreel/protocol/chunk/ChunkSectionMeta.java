package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record ChunkSectionMeta(@NotNull Identifier worldId, int x, int y, int z) {
}
