package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityMetadataDeltaCodec implements Codec<EntityMetadataDelta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityMetadataDelta delta) {
        buffer.writeInt(delta.index());
        EntityMetadataValue.CODEC.encode(buffer, delta.before());
        EntityMetadataValue.CODEC.encode(buffer, delta.after());
    }

    @Override
    public @NotNull EntityMetadataDelta decode(@NotNull ByteBuf buffer) {
        return new EntityMetadataDelta(buffer.readInt(), EntityMetadataValue.CODEC.decode(buffer), EntityMetadataValue.CODEC.decode(buffer));
    }

}
