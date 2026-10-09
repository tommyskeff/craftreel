package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChunkSectionBlockEntitiesStateCodec implements Codec<ChunkSectionBlockEntities> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChunkSectionBlockEntities state) {
        Map<Integer, byte[]> entries = state.entries();
        buffer.writeInt(entries.size());
        for (Map.Entry<Integer, byte[]> entry : entries.entrySet()) {
            buffer.writeShort(entry.getKey());
            buffer.writeInt(entry.getValue().length);
            buffer.writeBytes(entry.getValue());
        }
    }

    @Override
    public @NotNull ChunkSectionBlockEntities decode(@NotNull ByteBuf buffer) {
        int count = buffer.readInt();
        Map<Integer, byte[]> entries = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            int index = buffer.readUnsignedShort();
            byte[] nbt = new byte[buffer.readInt()];
            buffer.readBytes(nbt);
            entries.put(index, nbt);
        }
        return new ChunkSectionBlockEntities(entries);
    }

}
