package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.replay.reference.ReplayPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectatorCameraEvent extends ReplayEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final @Nullable ReplayPlayer target;

    public SpectatorCameraEvent(@NotNull MinecraftReplay replay, @NotNull Player player,
                                @Nullable ReplayPlayer target) {
        super(replay);
        this.player = player;
        this.target = target;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @Nullable ReplayPlayer getTarget() {
        return target;
    }

    public boolean isAttached() {
        return target != null;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
