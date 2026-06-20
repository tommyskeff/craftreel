package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityVelocityCodec implements Codec<EntityVelocity> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityVelocity velocity) {
        buffer.writeDouble(velocity.x()).writeDouble(velocity.y()).writeDouble(velocity.z());
    }

    @Override
    public @NotNull EntityVelocity decode(@NotNull ByteBuf buffer) {
        return new EntityVelocity(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

}
