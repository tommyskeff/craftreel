package dev.tommyjs.craftreel.protocol.scoreboard;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ScoreboardMetaCodec implements Codec<ScoreboardMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ScoreboardMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull ScoreboardMeta decode(@NotNull ByteBuf buffer) {
        return new ScoreboardMeta(Identifier.CODEC.decode(buffer));
    }

}
