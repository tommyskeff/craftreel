package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.UuidCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityCollectCodec implements Codec<EntityCollect> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityCollect collect) {
        UuidCodec.INSTANCE.encode(buffer, collect.collectorId());
        buffer.writeInt(collect.amount());
    }

    @Override
    public @NotNull EntityCollect decode(@NotNull ByteBuf buffer) {
        return new EntityCollect(UuidCodec.INSTANCE.decode(buffer), buffer.readInt());
    }

}
