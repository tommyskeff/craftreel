package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class TabHeaderMetaCodec implements Codec<TabHeaderMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TabHeaderMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull TabHeaderMeta decode(@NotNull ByteBuf buffer) {
        return new TabHeaderMeta(Identifier.CODEC.decode(buffer));
    }

}
