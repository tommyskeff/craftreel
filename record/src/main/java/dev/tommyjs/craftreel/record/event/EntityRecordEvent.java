package dev.tommyjs.craftreel.record.event;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EntityRecordEvent extends RecordingEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final @NotNull Entity entity;
    private boolean cancelled;

    public EntityRecordEvent(@NotNull MinecraftRecording recording, @NotNull Entity entity) {
        super(recording);
        this.entity = entity;
    }

    public @NotNull Entity getEntity() {
        return entity;
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
