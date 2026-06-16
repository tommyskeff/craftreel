package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class SoundEventCodec implements Codec<SoundEvent> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull SoundEvent event) {
        StringCodec.INSTANCE.encode(buffer, event.sound());
        buffer.writeDouble(event.x()).writeDouble(event.y()).writeDouble(event.z());
        buffer.writeFloat(event.volume()).writeFloat(event.pitch());
    }

    @Override
    public @NotNull SoundEvent decode(@NotNull ByteBuf buffer) {
        return new SoundEvent(StringCodec.INSTANCE.decode(buffer),
            buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readFloat());
    }

}
