package dev.tommyjs.craftreel.record.event;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WorldRecordEvent extends RecordingEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private boolean cancelled;

    public WorldRecordEvent(@NotNull MinecraftRecording recording, @NotNull World world) {
        super(recording);
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
