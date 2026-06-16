package dev.tommyjs.craftreel.util;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Identifier(@NotNull String namespace, @NotNull String path) {

    public static final Codec<Identifier> CODEC = new Codec<>() {
        @Override
        public void encode(@NotNull ByteBuf out, @NotNull Identifier id) {
            StringCodec.INSTANCE.encode(out, id.namespace());
            StringCodec.INSTANCE.encode(out, id.path());
        }

        @Override
        public @NotNull Identifier decode(@NotNull ByteBuf in) {
            return new Identifier(StringCodec.INSTANCE.decode(in), StringCodec.INSTANCE.decode(in));
        }
    };

    public static @NotNull Identifier of(@NotNull String namespace, @NotNull String path) {
        return new Identifier(namespace, path);
    }

    public static @NotNull Identifier of(@NotNull String identifier) {
        int index = identifier.indexOf(':');
        if (index < 0) {
            throw new IllegalArgumentException("missing ':' separator in context id: " + identifier);
        }

        return new Identifier(identifier.substring(0, index), identifier.substring(index + 1));
    }

    public static @NotNull Identifier random(@NotNull String namespace) {
        return new Identifier(namespace, UUID.randomUUID().toString());
    }

    @Override
    public @NotNull String toString() {
        return namespace + ":" + path;
    }

}
