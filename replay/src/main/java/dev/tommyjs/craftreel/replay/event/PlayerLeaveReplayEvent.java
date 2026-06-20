package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLeaveReplayEvent extends ReplayEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final @NotNull Player player;

    public PlayerLeaveReplayEvent(@NotNull MinecraftReplay replay, @NotNull Player player) {
        super(replay);
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
