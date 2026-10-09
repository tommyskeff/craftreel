package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChunkSectionBlockEntitiesModel
    implements TrackModel<ChunkSectionBlockEntities, ChunkSectionBlockEntityDelta> {

    @Override
    public @NotNull ChunkSectionBlockEntities applyDelta(@NotNull ChunkSectionBlockEntities state,
                                                         @NotNull ChunkSectionBlockEntityDelta delta) {
        if (delta.after() == null) {
            state.entries().remove(delta.index());
        } else {
            state.entries().put(delta.index(), delta.after());
        }
        return state;
    }

    @Override
    public @NotNull ChunkSectionBlockEntities cloneState(@NotNull ChunkSectionBlockEntities state) {
        return new ChunkSectionBlockEntities(state.entries());
    }

    @Override
    public @NotNull List<ChunkSectionBlockEntityDelta> condense(@NotNull ChunkSectionBlockEntities state,
                                                                @NotNull List<ChunkSectionBlockEntityDelta> deltas) {
        Map<Integer, ChunkSectionBlockEntityDelta> merged = new LinkedHashMap<>();
        for (ChunkSectionBlockEntityDelta delta : deltas) {
            merged.merge(delta.index(), delta,
                (existing, next) -> new ChunkSectionBlockEntityDelta(existing.index(), existing.before(), next.after()));
        }
        return List.copyOf(merged.values());
    }

    @Override
    public @NotNull ChunkSectionBlockEntityDelta reverse(@NotNull ChunkSectionBlockEntityDelta delta) {
        return new ChunkSectionBlockEntityDelta(delta.index(), delta.after(), delta.before());
    }

}
