package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EnvironmentStateCodec implements Codec<EnvironmentState> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EnvironmentState state) {
        Identifier.CODEC.encode(buffer, state.worldId());
        buffer.writeLong(state.time());
        buffer.writeBoolean(state.storm());
        buffer.writeBoolean(state.thundering());
    }

    @Override
    public @NotNull EnvironmentState decode(@NotNull ByteBuf buffer) {
        Identifier worldId = Identifier.CODEC.decode(buffer);
        return new EnvironmentState(worldId, buffer.readLong(), buffer.readBoolean(), buffer.readBoolean());
    }

}
