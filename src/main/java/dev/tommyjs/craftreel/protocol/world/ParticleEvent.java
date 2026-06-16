package dev.tommyjs.craftreel.protocol.world;

import org.jetbrains.annotations.NotNull;

public record ParticleEvent(@NotNull String particle, double x, double y, double z,
                            float offsetX, float offsetY, float offsetZ, float speed, int count) {
}
