package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class TabMetaCodec implements Codec<TabMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TabMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull TabMeta decode(@NotNull ByteBuf buffer) {
        return new TabMeta(Identifier.CODEC.decode(buffer));
    }

}
