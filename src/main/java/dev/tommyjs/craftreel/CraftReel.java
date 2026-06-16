package dev.tommyjs.craftreel;

import dev.tommyjs.craftreel.record.MinecraftRecordingBuilder;
import dev.tommyjs.craftreel.replay.MinecraftReplayBuilder;

public final class CraftReel {

    public static final String NAMESPACE = "craftreel";

    private CraftReel() {
    }

    public static MinecraftReplayBuilder replay() {
        return new MinecraftReplayBuilder();
    }

    public static MinecraftRecordingBuilder record() {
        return new MinecraftRecordingBuilder();
    }

}
