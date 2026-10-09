package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkSectionBlockEntityDeltaCodec implements Codec<ChunkSectionBlockEntityDelta> {

    private static void encodeNbt(@NotNull ByteBuf buffer, byte @Nullable [] nbt) {
        if (nbt == null) {
            buffer.writeInt(-1);
            return;
        }
        buffer.writeInt(nbt.length);
        buffer.writeBytes(nbt);
    }

    private static byte @Nullable [] decodeNbt(@NotNull ByteBuf buffer) {
        int length = buffer.readInt();
        if (length < 0) {
            return null;
        }
        byte[] nbt = new byte[length];
        buffer.readBytes(nbt);
        return nbt;
    }

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChunkSectionBlockEntityDelta delta) {
        buffer.writeShort(delta.index());
        encodeNbt(buffer, delta.before());
        encodeNbt(buffer, delta.after());
    }

    @Override
    public @NotNull ChunkSectionBlockEntityDelta decode(@NotNull ByteBuf buffer) {
        return new ChunkSectionBlockEntityDelta(buffer.readUnsignedShort(), decodeNbt(buffer), decodeNbt(buffer));
    }

}
