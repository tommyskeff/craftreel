package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class WorldBorderStateCodec implements Codec<WorldBorderState> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull WorldBorderState state) {
        buffer.writeDouble(state.centerX());
        buffer.writeDouble(state.centerZ());
        buffer.writeDouble(state.diameter());
        buffer.writeDouble(state.targetDiameter());
        buffer.writeLong(state.remainingMillis());
        buffer.writeInt(state.warningBlocks());
        buffer.writeInt(state.warningSeconds());
    }

    @Override
    public @NotNull WorldBorderState decode(@NotNull ByteBuf buffer) {
        return new WorldBorderState(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readLong(), buffer.readInt(), buffer.readInt());
    }

}
