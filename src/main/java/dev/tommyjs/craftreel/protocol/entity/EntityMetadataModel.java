package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityMetadataModel implements TrackModel<EntityMetadata, EntityMetadataDelta> {

    @Override
    public @NotNull EntityMetadata applyDelta(@NotNull EntityMetadata state, @NotNull EntityMetadataDelta delta) {
        state.values().put(delta.index(), delta.after());
        return state;
    }

    @Override
    public @NotNull EntityMetadata cloneState(@NotNull EntityMetadata state) {
        return new EntityMetadata(state.values());
    }

    @Override
    public @NotNull List<EntityMetadataDelta> condense(@NotNull EntityMetadata ref, @NotNull List<EntityMetadataDelta> deltas) {
        return List.copyOf(deltas);
    }

    @Override
    public @NotNull EntityMetadataDelta reverse(@NotNull EntityMetadataDelta delta) {
        return new EntityMetadataDelta(delta.index(), delta.after(), delta.before());
    }

}
