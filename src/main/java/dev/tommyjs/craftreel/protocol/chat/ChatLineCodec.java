package dev.tommyjs.craftreel.protocol.chat;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChatLineCodec implements Codec<ChatLine> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChatLine line) {
        ComponentCodec.INSTANCE.encode(buffer, line.text());
    }

    @Override
    public @NotNull ChatLine decode(@NotNull ByteBuf buffer) {
        return new ChatLine(ComponentCodec.INSTANCE.decode(buffer));
    }

}
