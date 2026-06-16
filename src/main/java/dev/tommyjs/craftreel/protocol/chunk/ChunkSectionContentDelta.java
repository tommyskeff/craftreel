package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.dynworld.block.BlockState;
import org.jetbrains.annotations.NotNull;

public sealed interface ChunkSectionContentDelta
    permits ChunkSectionContentDelta.BlockDelta, ChunkSectionContentDelta.MultiBlockDelta {

    record BlockDelta(int x, int y, int z, @NotNull BlockState before, @NotNull BlockState after)
        implements ChunkSectionContentDelta {
    }

    record MultiBlockDelta(@NotNull BlockDelta[] blocks) implements ChunkSectionContentDelta {
    }

}
