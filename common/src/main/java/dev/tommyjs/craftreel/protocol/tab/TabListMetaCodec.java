package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class TabListMetaCodec implements Codec<TabListMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TabListMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull TabListMeta decode(@NotNull ByteBuf buffer) {
        return new TabListMeta(Identifier.CODEC.decode(buffer));
    }

}
