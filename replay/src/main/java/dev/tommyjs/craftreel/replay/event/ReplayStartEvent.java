package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ReplayStartEvent extends ReplayEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public ReplayStartEvent(@NotNull MinecraftReplay replay) {
        super(replay);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
