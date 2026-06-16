package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class WorldEventCodec implements Codec<WorldEvent> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull WorldEvent event) {
        buffer.writeInt(event.type());
        buffer.writeInt(event.x()).writeInt(event.y()).writeInt(event.z());
        buffer.writeInt(event.data());
    }

    @Override
    public @NotNull WorldEvent decode(@NotNull ByteBuf buffer) {
        return new WorldEvent(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

}
