package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityPoseDeltaCodec implements Codec<EntityPoseDelta> {

    @Override
    public void encode(@NotNull ByteBuf out, @NotNull EntityPoseDelta d) {
        out.writeDouble(d.dx()).writeDouble(d.dy()).writeDouble(d.dz())
            .writeFloat(d.dyaw()).writeFloat(d.dpitch()).writeFloat(d.dheadYaw());
    }

    @Override
    public @NotNull EntityPoseDelta decode(@NotNull ByteBuf in) {
        return new EntityPoseDelta(in.readDouble(), in.readDouble(), in.readDouble(),
            in.readFloat(), in.readFloat(), in.readFloat());
    }

}
