package dev.tommyjs.craftreel.protocol.text;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class TextContextCodec implements Codec<TextContext> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TextContext meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull TextContext decode(@NotNull ByteBuf buffer) {
        return new TextContext(Identifier.CODEC.decode(buffer));
    }

}
