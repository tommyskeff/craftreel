package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class TabHeaderCodec implements Codec<TabHeader> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TabHeader header) {
        ComponentCodec.INSTANCE.encode(buffer, header.header());
        ComponentCodec.INSTANCE.encode(buffer, header.footer());
    }

    @Override
    public @NotNull TabHeader decode(@NotNull ByteBuf buffer) {
        Component header = ComponentCodec.INSTANCE.decode(buffer);
        Component footer = ComponentCodec.INSTANCE.decode(buffer);
        return new TabHeader(header, footer);
    }

}
