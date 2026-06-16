package dev.tommyjs.craftreel.protocol.sidebar;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SidebarStateCodec implements Codec<SidebarState> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull SidebarState state) {
        List<Component> lines = new ArrayList<>(state.lines());
        buffer.writeInt(lines.size());
        for (Component line : lines) {
            ComponentCodec.INSTANCE.encode(buffer, line);
        }
    }

    @Override
    public @NotNull SidebarState decode(@NotNull ByteBuf buffer) {
        int count = buffer.readInt();
        List<Component> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            lines.add(ComponentCodec.INSTANCE.decode(buffer));
        }
        return new SidebarState(lines);
    }

}
