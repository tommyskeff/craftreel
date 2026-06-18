package dev.tommyjs.craftreel.replay.reference;

import dev.tommyjs.dynworld.entity.EntityTracker;
import dev.tommyjs.dynworld.world.DynamicWorld;
import org.jetbrains.annotations.NotNull;

public interface WorldContext extends ContextGroup {

    @NotNull DynamicWorld world();

    @NotNull EntityTracker tracker();

}
