package dev.tommyjs.craftreel.protocol.scoreboard;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ObjectiveInfoCodec implements Codec<ObjectiveInfo> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ObjectiveInfo info) {
        ComponentCodec.INSTANCE.encode(buffer, info.displayName());
        buffer.writeByte(info.renderType().ordinal());
        Integer slot = info.slot();
        buffer.writeBoolean(slot != null);
        if (slot != null) {
            buffer.writeInt(slot);
        }
    }

    @Override
    public @NotNull ObjectiveInfo decode(@NotNull ByteBuf buffer) {
        Component displayName = ComponentCodec.INSTANCE.decode(buffer);
        ScoreboardRenderType renderType = ScoreboardRenderType.values()[buffer.readByte()];
        Integer slot = buffer.readBoolean() ? buffer.readInt() : null;
        return new ObjectiveInfo(displayName, renderType, slot);
    }

}
