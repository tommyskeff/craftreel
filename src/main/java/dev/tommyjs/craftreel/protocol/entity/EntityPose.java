package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.NotNull;

public record EntityPose(double x, double y, double z, float yaw, float pitch, float headYaw) {

    public @NotNull EntityPoseDelta to(@NotNull EntityPose other) {
        return new EntityPoseDelta(other.x - x, other.y - y, other.z - z,
            other.yaw - yaw, other.pitch - pitch, other.headYaw - headYaw);
    }

}
