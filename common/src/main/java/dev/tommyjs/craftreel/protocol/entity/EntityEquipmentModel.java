package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityEquipmentModel implements TrackModel<EntityEquipment, EntityEquipmentDelta> {

    @Override
    public @NotNull EntityEquipment applyDelta(@NotNull EntityEquipment state, @NotNull EntityEquipmentDelta delta) {
        state.items().put(delta.slot(), delta.after());
        return state;
    }

    @Override
    public @NotNull EntityEquipment cloneState(@NotNull EntityEquipment state) {
        return new EntityEquipment(state.items());
    }

    @Override
    public @NotNull List<EntityEquipmentDelta> condense(@NotNull EntityEquipment ref, @NotNull List<EntityEquipmentDelta> deltas) {
        return List.copyOf(deltas);
    }

    @Override
    public @NotNull EntityEquipmentDelta reverse(@NotNull EntityEquipmentDelta delta) {
        return new EntityEquipmentDelta(delta.slot(), delta.after(), delta.before());
    }

}
