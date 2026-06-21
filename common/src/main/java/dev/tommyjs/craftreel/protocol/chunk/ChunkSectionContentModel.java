package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkSectionContentModel implements TrackModel<ChunkSectionContent, ChunkSectionContentDelta> {

    @Override
    public @NotNull ChunkSectionContent applyDelta(@NotNull ChunkSectionContent state, @NotNull ChunkSectionContentDelta delta) {
        if (delta instanceof ChunkSectionContentDelta.BlockDelta singleBlockDelta) {
            setBlock(state.section(), singleBlockDelta);
            return state;
        } else if (delta instanceof ChunkSectionContentDelta.MultiBlockDelta multiBlockDelta) {
            for (ChunkSectionContentDelta.BlockDelta blockDelta : multiBlockDelta.blocks()) {
                setBlock(state.section(), blockDelta);
            }
            return state;
        } else {
            throw new IllegalStateException("Unknown delta: " + delta);
        }
    }

    private static void setBlock(char[] section, ChunkSectionContentDelta.BlockDelta block) {
        int index = ((block.y() & 0xF) << 8) | ((block.z() & 0xF) << 4) | (block.x() & 0xF);
        section[index] = (char) ((block.after().id() << 4) | block.after().data());
    }

    @Override
    public @NotNull ChunkSectionContent cloneState(@NotNull ChunkSectionContent state) {
        return new ChunkSectionContent(state.section().clone());
    }

    @Override
    public @NotNull List<ChunkSectionContentDelta> condense(@NotNull ChunkSectionContent state,
                                                            @NotNull List<ChunkSectionContentDelta> list) {
        Map<Long, ChunkSectionContentDelta.BlockDelta> deltas = new HashMap<>();
        for (ChunkSectionContentDelta delta : list) {
            if (delta instanceof ChunkSectionContentDelta.BlockDelta singleBlockDelta) {
                mergeDelta(deltas, singleBlockDelta);
            } else if (delta instanceof ChunkSectionContentDelta.MultiBlockDelta multiBlockDelta) {
                for (ChunkSectionContentDelta.BlockDelta blockDelta : multiBlockDelta.blocks()) {
                    mergeDelta(deltas, blockDelta);
                }
            } else {
                throw new IllegalStateException("Unknown delta: " + delta);
            }
        }

        return List.copyOf(deltas.values());
    }

    private void mergeDelta(Map<Long, ChunkSectionContentDelta.BlockDelta> deltas,
                            ChunkSectionContentDelta.BlockDelta singleBlockDelta) {
        long key = ((long) singleBlockDelta.x() << 32) | ((long) singleBlockDelta.y() << 16) | singleBlockDelta.z();
        if (deltas.containsKey(key)) {
            deltas.computeIfPresent(key,
                (ignoredKey, existing) -> new ChunkSectionContentDelta.BlockDelta(existing.x(), existing.y(), existing.z(),
                    existing.before(), singleBlockDelta.after()));
        } else {
            deltas.put(key, singleBlockDelta);
        }
    }

    @Override
    public @NotNull ChunkSectionContentDelta reverse(@NotNull ChunkSectionContentDelta delta) {
        if (delta instanceof ChunkSectionContentDelta.BlockDelta singleBlockDelta) {
            return new ChunkSectionContentDelta.BlockDelta(
                singleBlockDelta.x(), singleBlockDelta.y(), singleBlockDelta.z(), singleBlockDelta.after(),
                singleBlockDelta.before());
        } else if (delta instanceof ChunkSectionContentDelta.MultiBlockDelta multiBlockDelta) {
            ChunkSectionContentDelta.BlockDelta[] reversed =
                new ChunkSectionContentDelta.BlockDelta[multiBlockDelta.blocks().length];
            for (int i = 0; i < multiBlockDelta.blocks().length; i++) {
                ChunkSectionContentDelta.BlockDelta blockDelta = multiBlockDelta.blocks()[i];
                reversed[i] = new ChunkSectionContentDelta.BlockDelta(blockDelta.x(), blockDelta.y(), blockDelta.z(),
                    blockDelta.after(), blockDelta.before());
            }
            return new ChunkSectionContentDelta.MultiBlockDelta(reversed);
        } else {
            throw new IllegalStateException("Unknown delta: " + delta);
        }
    }

}
