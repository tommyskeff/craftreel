package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ParticleEventCodec implements Codec<ParticleEvent> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ParticleEvent event) {
        StringCodec.INSTANCE.encode(buffer, event.particle());
        buffer.writeDouble(event.x()).writeDouble(event.y()).writeDouble(event.z());
        buffer.writeFloat(event.offsetX()).writeFloat(event.offsetY()).writeFloat(event.offsetZ());
        buffer.writeFloat(event.speed()).writeInt(event.count());
    }

    @Override
    public @NotNull ParticleEvent decode(@NotNull ByteBuf buffer) {
        return new ParticleEvent(StringCodec.INSTANCE.decode(buffer),
            buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }

}
