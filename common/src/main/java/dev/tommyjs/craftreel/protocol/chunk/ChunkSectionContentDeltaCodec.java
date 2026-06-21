package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.craftreel.protocol.block.BlockState;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChunkSectionContentDeltaCodec implements Codec<ChunkSectionContentDelta> {

    private static void encodeBlock(@NotNull ByteBuf buffer, ChunkSectionContentDelta.BlockDelta singleBlockDelta) {
        buffer.writeInt(singleBlockDelta.x());
        buffer.writeInt(singleBlockDelta.y());
        buffer.writeInt(singleBlockDelta.z());
        buffer.writeShort(singleBlockDelta.before().id());
        buffer.writeByte(singleBlockDelta.before().data());
        buffer.writeShort(singleBlockDelta.after().id());
        buffer.writeByte(singleBlockDelta.after().data());
    }

    private static @NotNull ChunkSectionContentDelta.BlockDelta decodeBlock(@NotNull ByteBuf buffer) {
        return new ChunkSectionContentDelta.BlockDelta(buffer.readInt(), buffer.readInt(), buffer.readInt(),
            BlockState.of(buffer.readShort(), buffer.readByte()), BlockState.of(buffer.readShort(), buffer.readByte()));
    }

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChunkSectionContentDelta delta) {
        if (delta instanceof ChunkSectionContentDelta.BlockDelta singleBlockDelta) {
            buffer.writeByte(0);
            encodeBlock(buffer, singleBlockDelta);
        } else if (delta instanceof ChunkSectionContentDelta.MultiBlockDelta multiBlockDelta) {
            buffer.writeByte(1);
            buffer.writeInt(multiBlockDelta.blocks().length);
            for (ChunkSectionContentDelta.BlockDelta blockDelta : multiBlockDelta.blocks()) {
                encodeBlock(buffer, blockDelta);
            }
        } else {
            throw new IllegalStateException("Unknown delta: " + delta);
        }
    }

    @Override
    public @NotNull ChunkSectionContentDelta decode(@NotNull ByteBuf buffer) {
        int type = buffer.readByte();
        return switch (type) {
            case 0 -> decodeBlock(buffer);
            case 1 -> {
                int length = buffer.readInt();
                ChunkSectionContentDelta.BlockDelta[] blocks = new ChunkSectionContentDelta.BlockDelta[length];
                for (int i = 0; i < length; i++) {
                    blocks[i] = decodeBlock(buffer);
                }
                yield new ChunkSectionContentDelta.MultiBlockDelta(blocks);
            }
            default -> throw new IllegalStateException("Unknown delta type: " + type);
        };
    }

}
