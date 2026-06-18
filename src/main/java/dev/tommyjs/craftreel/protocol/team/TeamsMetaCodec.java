package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class TeamsMetaCodec implements Codec<TeamsMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TeamsMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull TeamsMeta decode(@NotNull ByteBuf buffer) {
        return new TeamsMeta(Identifier.CODEC.decode(buffer));
    }

}
