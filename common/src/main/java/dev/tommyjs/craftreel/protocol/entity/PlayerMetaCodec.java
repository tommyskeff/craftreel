package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import dev.tommyjs.reel.track.codec.UuidCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerMetaCodec implements Codec<PlayerMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull PlayerMeta meta) {
        UuidCodec.INSTANCE.encode(buffer, meta.id());
        Identifier.CODEC.encode(buffer, meta.worldId());
        UuidCodec.INSTANCE.encode(buffer, meta.profileId());
        StringCodec.INSTANCE.encode(buffer, meta.name());
        writeOptionalString(buffer, meta.skinValue());
        writeOptionalString(buffer, meta.skinSignature());
    }

    @Override
    public @NotNull PlayerMeta decode(@NotNull ByteBuf buffer) {
        UUID id = UuidCodec.INSTANCE.decode(buffer);
        Identifier worldId = Identifier.CODEC.decode(buffer);
        UUID profileId = UuidCodec.INSTANCE.decode(buffer);
        String name = StringCodec.INSTANCE.decode(buffer);
        String skinValue = readOptionalString(buffer);
        String skinSignature = readOptionalString(buffer);
        return new PlayerMeta(id, worldId, profileId, name, skinValue, skinSignature);
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
