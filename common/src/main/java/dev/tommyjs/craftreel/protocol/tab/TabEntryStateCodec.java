package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabEntryStateCodec implements Codec<TabEntryState> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TabEntryState state) {
        buffer.writeInt(state.latency());
        buffer.writeInt(state.gameMode());
        Component displayName = state.displayName();
        buffer.writeBoolean(displayName != null);
        if (displayName != null) {
            ComponentCodec.INSTANCE.encode(buffer, displayName);
        }
    }

    @Override
    public @NotNull TabEntryState decode(@NotNull ByteBuf buffer) {
        int latency = buffer.readInt();
        int gameMode = buffer.readInt();
        Component displayName = buffer.readBoolean() ? ComponentCodec.INSTANCE.decode(buffer) : null;
        return new TabEntryState(latency, gameMode, displayName);
    }

}
