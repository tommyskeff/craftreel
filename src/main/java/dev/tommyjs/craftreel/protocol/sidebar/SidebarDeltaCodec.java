package dev.tommyjs.craftreel.protocol.sidebar;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class SidebarDeltaCodec implements Codec<SidebarDelta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull SidebarDelta delta) {
        if (delta instanceof SidebarDelta.SetLine set) {
            buffer.writeByte(0);
            buffer.writeInt(set.index());
            ComponentCodec.INSTANCE.encode(buffer, set.before());
            ComponentCodec.INSTANCE.encode(buffer, set.after());
        } else if (delta instanceof SidebarDelta.AddLine add) {
            buffer.writeByte(1);
            buffer.writeInt(add.index());
            ComponentCodec.INSTANCE.encode(buffer, add.text());
        } else if (delta instanceof SidebarDelta.RemoveLine remove) {
            buffer.writeByte(2);
            buffer.writeInt(remove.index());
            ComponentCodec.INSTANCE.encode(buffer, remove.text());
        } else {
            throw new IllegalStateException("Unknown sidebar delta: " + delta);
        }
    }

    @Override
    public @NotNull SidebarDelta decode(@NotNull ByteBuf buffer) {
        int type = buffer.readByte();
        return switch (type) {
            case 0 -> new SidebarDelta.SetLine(buffer.readInt(), ComponentCodec.INSTANCE.decode(buffer),
                ComponentCodec.INSTANCE.decode(buffer));
            case 1 -> new SidebarDelta.AddLine(buffer.readInt(), ComponentCodec.INSTANCE.decode(buffer));
            case 2 -> new SidebarDelta.RemoveLine(buffer.readInt(), ComponentCodec.INSTANCE.decode(buffer));
            default -> throw new IllegalStateException("Unknown sidebar delta type: " + type);
        };
    }

}
