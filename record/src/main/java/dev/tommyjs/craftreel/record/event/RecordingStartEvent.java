package dev.tommyjs.craftreel.record.event;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RecordingStartEvent extends RecordingEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public RecordingStartEvent(@NotNull MinecraftRecording recording) {
        super(recording);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
