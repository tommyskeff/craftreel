package dev.tommyjs.craftreel.replay.handler;

import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SpectatorRegistry {

    @NotNull Collection<ReplaySpectator> spectators();

    void purge(@NotNull ViewerContext context);

}
