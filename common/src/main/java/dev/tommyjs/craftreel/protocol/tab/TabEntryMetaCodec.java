package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import dev.tommyjs.reel.track.codec.UuidCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TabEntryMetaCodec implements Codec<TabEntryMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TabEntryMeta meta) {
        Identifier.CODEC.encode(buffer, meta.contextId());
        UuidCodec.INSTANCE.encode(buffer, meta.profileId());
        StringCodec.INSTANCE.encode(buffer, meta.name());
        writeOptionalString(buffer, meta.skinValue());
        writeOptionalString(buffer, meta.skinSignature());
    }

    @Override
    public @NotNull TabEntryMeta decode(@NotNull ByteBuf buffer) {
        Identifier contextId = Identifier.CODEC.decode(buffer);
        UUID profileId = UuidCodec.INSTANCE.decode(buffer);
        String name = StringCodec.INSTANCE.decode(buffer);
        String skinValue = readOptionalString(buffer);
        String skinSignature = readOptionalString(buffer);
        return new TabEntryMeta(contextId, profileId, name, skinValue, skinSignature);
    }

    private static void writeOptionalString(ByteBuf buffer, @Nullable String value) {
        buffer.writeBoolean(value != null);
        if (value != null) {
            StringCodec.INSTANCE.encode(buffer, value);
        }
    }

    private static @Nullable String readOptionalString(ByteBuf buffer) {
        return buffer.readBoolean() ? StringCodec.INSTANCE.decode(buffer) : null;
    }

}
