package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityAnimationCodec implements Codec<EntityAnimationType> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityAnimationType type) {
        buffer.writeByte(type.ordinal());
    }

    @Override
    public @NotNull EntityAnimationType decode(@NotNull ByteBuf buffer) {
        return EntityAnimationType.values()[buffer.readByte()];
    }

}
