package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ExplosionEventCodec implements Codec<ExplosionEvent> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ExplosionEvent event) {
        buffer.writeDouble(event.x()).writeDouble(event.y()).writeDouble(event.z());
        buffer.writeFloat(event.strength());
    }

    @Override
    public @NotNull ExplosionEvent decode(@NotNull ByteBuf buffer) {
        return new ExplosionEvent(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat());
    }

}
