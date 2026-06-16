package dev.tommyjs.craftreel.protocol.text;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Codec for {@link Component} text. Components are serialized to compact JSON via
 * {@link GsonComponentSerializer}, which preserves the full style tree (hex colors,
 * click/hover events, fonts) losslessly. Downconversion to legacy section strings for
 * older clients is left to the consumer at send time.
 */
public final class ComponentCodec implements Codec<Component> {

    public static final ComponentCodec INSTANCE = new ComponentCodec();

    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    private ComponentCodec() {
    }

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull Component component) {
        StringCodec.INSTANCE.encode(buffer, GSON.serialize(component));
    }

    @Override
    public @NotNull Component decode(@NotNull ByteBuf buffer) {
        return GSON.deserialize(StringCodec.INSTANCE.decode(buffer));
    }

}
