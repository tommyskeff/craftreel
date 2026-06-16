package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityPoseStateCodec implements Codec<EntityPose> {

    @Override
    public void encode(@NotNull ByteBuf out, @NotNull EntityPose s) {
        out.writeDouble(s.x()).writeDouble(s.y()).writeDouble(s.z())
            .writeFloat(s.yaw()).writeFloat(s.pitch()).writeFloat(s.headYaw());
    }

    @Override
    public @NotNull EntityPose decode(@NotNull ByteBuf in) {
        return new EntityPose(in.readDouble(), in.readDouble(), in.readDouble(),
            in.readFloat(), in.readFloat(), in.readFloat());
    }

}
