package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class ReplayEvent extends Event {

    private final @NotNull MinecraftReplay replay;

    protected ReplayEvent(@NotNull MinecraftReplay replay) {
        this.replay = replay;
    }

    public @NotNull MinecraftReplay getReplay() {
        return replay;
    }

}
