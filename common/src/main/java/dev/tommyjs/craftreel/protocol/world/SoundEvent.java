package dev.tommyjs.craftreel.protocol.world;

import org.jetbrains.annotations.NotNull;

public record SoundEvent(@NotNull String sound, double x, double y, double z, float volume, float pitch) {
}
