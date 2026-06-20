package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.TrackModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityPoseModel implements TrackModel<EntityPose, EntityPoseDelta> {

    @Override
    public @NotNull EntityPose applyDelta(@NotNull EntityPose s, @NotNull EntityPoseDelta d) {
        return new EntityPose(s.x() + d.dx(), s.y() + d.dy(), s.z() + d.dz(),
            s.yaw() + d.dyaw(), s.pitch() + d.dpitch(), s.headYaw() + d.dheadYaw());
    }

    @Override
    public @NotNull EntityPose cloneState(@NotNull EntityPose s) {
        return s;
    }

    @Override
    public @NotNull List<EntityPoseDelta> condense(@NotNull EntityPose ref, @NotNull List<EntityPoseDelta> deltas) {
        double dx = 0, dy = 0, dz = 0;
        float dyaw = 0, dpitch = 0, dheadYaw = 0;
        for (EntityPoseDelta d : deltas) {
            dx += d.dx(); dy += d.dy(); dz += d.dz();
            dyaw += d.dyaw(); dpitch += d.dpitch(); dheadYaw += d.dheadYaw();
        }
        return List.of(new EntityPoseDelta(dx, dy, dz, dyaw, dpitch, dheadYaw));
    }

    @Override
    public @NotNull EntityPoseDelta reverse(@NotNull EntityPoseDelta d) {
        return new EntityPoseDelta(-d.dx(), -d.dy(), -d.dz(), -d.dyaw(), -d.dpitch(), -d.dheadYaw());
    }

}
