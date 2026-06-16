package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityPresenceStateCodec implements Codec<EntityPresence> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityPresence state) {
        buffer.writeBoolean(state.present());
    }

    @Override
    public @NotNull EntityPresence decode(@NotNull ByteBuf buffer) {
        return new EntityPresence(buffer.readBoolean());
    }

}
