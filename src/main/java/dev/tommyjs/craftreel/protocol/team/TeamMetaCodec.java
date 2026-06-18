package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class TeamMetaCodec implements Codec<TeamMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TeamMeta meta) {
        Identifier.CODEC.encode(buffer, meta.contextId());
        StringCodec.INSTANCE.encode(buffer, meta.name());
    }

    @Override
    public @NotNull TeamMeta decode(@NotNull ByteBuf buffer) {
        return new TeamMeta(Identifier.CODEC.decode(buffer), StringCodec.INSTANCE.decode(buffer));
    }

}
