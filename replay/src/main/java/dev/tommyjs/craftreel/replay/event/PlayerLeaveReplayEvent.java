package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.replay.ViewerHandover;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLeaveReplayEvent extends ReplayEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final @NotNull Player player;
    private final @Nullable ViewerHandover handover;

    public PlayerLeaveReplayEvent(@NotNull MinecraftReplay replay, @NotNull Player player) {
        this(replay, player, null);
    }

    public PlayerLeaveReplayEvent(@NotNull MinecraftReplay replay, @NotNull Player player,
                                  @Nullable ViewerHandover handover) {
        super(replay);
        this.player = player;
        this.handover = handover;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @Nullable ViewerHandover getHandover() {
        return handover;
    }

    public boolean isTransfer() {
        return handover != null;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
