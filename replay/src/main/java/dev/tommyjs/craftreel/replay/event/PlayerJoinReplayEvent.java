package dev.tommyjs.craftreel.replay.event;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.replay.ReplaySpectator;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerJoinReplayEvent extends ReplayEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ReplaySpectator spectator;

    public PlayerJoinReplayEvent(MinecraftReplay replay, Player player, ReplaySpectator spectator) {
        super(replay);
        this.player = player;
        this.spectator = spectator;
    }

    public Player getPlayer() {
        return player;
    }

    public ReplaySpectator getSpectator() {
        return spectator;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
