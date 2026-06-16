package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityMetadataStateCodec implements Codec<EntityMetadata> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityMetadata state) {
        Map<Integer, EntityMetadataValue> values = state.values();
        buffer.writeInt(values.size());
        for (Map.Entry<Integer, EntityMetadataValue> entry : values.entrySet()) {
            buffer.writeInt(entry.getKey());
            EntityMetadataValue.CODEC.encode(buffer, entry.getValue());
        }
    }

    @Override
    public @NotNull EntityMetadata decode(@NotNull ByteBuf buffer) {
        int count = buffer.readInt();
        Map<Integer, EntityMetadataValue> values = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            int index = buffer.readInt();
            values.put(index, EntityMetadataValue.CODEC.decode(buffer));
        }
        return new EntityMetadata(values);
    }

}
