package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChunkSectionMetaCodec implements Codec<ChunkSectionMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChunkSectionMeta meta) {
        Identifier.CODEC.encode(buffer, meta.worldId());
        buffer.writeInt(meta.x());
        buffer.writeInt(meta.y());
        buffer.writeInt(meta.z());
    }

    @Override
    public @NotNull ChunkSectionMeta decode(@NotNull ByteBuf buffer) {
        return new ChunkSectionMeta(Identifier.CODEC.decode(buffer), buffer.readInt(), buffer.readInt(),
            buffer.readInt());
    }

}
