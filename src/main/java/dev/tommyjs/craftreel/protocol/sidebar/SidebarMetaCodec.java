package dev.tommyjs.craftreel.protocol.sidebar;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class SidebarMetaCodec implements Codec<SidebarMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull SidebarMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
    }

    @Override
    public @NotNull SidebarMeta decode(@NotNull ByteBuf buffer) {
        return new SidebarMeta(Identifier.CODEC.decode(buffer));
    }

}
