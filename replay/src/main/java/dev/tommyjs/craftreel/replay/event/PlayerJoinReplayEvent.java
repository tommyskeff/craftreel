package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.ViewerHandover;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinReplayEvent extends ReplayEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ReplaySpectator spectator;
    private final @Nullable ViewerHandover handover;

    public PlayerJoinReplayEvent(MinecraftReplay replay, Player player, ReplaySpectator spectator) {
        this(replay, player, spectator, null);
    }

    public PlayerJoinReplayEvent(MinecraftReplay replay, Player player, ReplaySpectator spectator,
                                 @Nullable ViewerHandover handover) {
        super(replay);
        this.player = player;
        this.spectator = spectator;
        this.handover = handover;
    }

    public Player getPlayer() {
        return player;
    }

    public ReplaySpectator getSpectator() {
        return spectator;
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
