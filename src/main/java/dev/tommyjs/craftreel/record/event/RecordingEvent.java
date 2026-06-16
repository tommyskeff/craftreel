package dev.tommyjs.craftreel.record.event;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class RecordingEvent extends Event {

    private final @NotNull MinecraftRecording recording;

    protected RecordingEvent(@NotNull MinecraftRecording recording) {
        this.recording = recording;
    }

    public @NotNull MinecraftRecording getRecording() {
        return recording;
    }

}
