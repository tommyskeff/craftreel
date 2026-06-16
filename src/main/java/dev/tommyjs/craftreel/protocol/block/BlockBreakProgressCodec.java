package dev.tommyjs.craftreel.protocol.block;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class BlockBreakProgressCodec implements Codec<BlockBreakProgress> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull BlockBreakProgress event) {
        buffer.writeInt(event.sourceEntityId());
        buffer.writeInt(event.x()).writeInt(event.y()).writeInt(event.z());
        buffer.writeInt(event.progress());
    }

    @Override
    public @NotNull BlockBreakProgress decode(@NotNull ByteBuf buffer) {
        return new BlockBreakProgress(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

}
