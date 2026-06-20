package dev.tommyjs.craftreel.protocol.title;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class TitleCodec implements Codec<Title> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull Title title) {
        ComponentCodec.INSTANCE.encode(buffer, title.title());
        ComponentCodec.INSTANCE.encode(buffer, title.subtitle());
        buffer.writeInt(title.fadeIn());
        buffer.writeInt(title.stay());
        buffer.writeInt(title.fadeOut());
    }

    @Override
    public @NotNull Title decode(@NotNull ByteBuf buffer) {
        Component title = ComponentCodec.INSTANCE.decode(buffer);
        Component subtitle = ComponentCodec.INSTANCE.decode(buffer);
        int fadeIn = buffer.readInt();
        int stay = buffer.readInt();
        int fadeOut = buffer.readInt();
        return new Title(title, subtitle, fadeIn, stay, fadeOut);
    }

}
