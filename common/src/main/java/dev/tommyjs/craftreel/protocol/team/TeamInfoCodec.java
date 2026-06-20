package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.craftreel.protocol.text.ComponentCodec;
import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class TeamInfoCodec implements Codec<TeamInfo> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TeamInfo info) {
        StringCodec.INSTANCE.encode(buffer, info.name());
        ComponentCodec.INSTANCE.encode(buffer, info.displayName());
        ComponentCodec.INSTANCE.encode(buffer, info.prefix());
        ComponentCodec.INSTANCE.encode(buffer, info.suffix());
        NamedTextColor color = info.color();
        buffer.writeBoolean(color != null);
        if (color != null) {
            StringCodec.INSTANCE.encode(buffer, color.toString());
        }
        buffer.writeBoolean(info.friendlyFire());
        buffer.writeBoolean(info.seeFriendlyInvisibles());
        buffer.writeByte(info.nameTagVisibility().ordinal());
        buffer.writeByte(info.collisionRule().ordinal());
    }

    @Override
    public @NotNull TeamInfo decode(@NotNull ByteBuf buffer) {
        String name = StringCodec.INSTANCE.decode(buffer);
        Component displayName = ComponentCodec.INSTANCE.decode(buffer);
        Component prefix = ComponentCodec.INSTANCE.decode(buffer);
        Component suffix = ComponentCodec.INSTANCE.decode(buffer);
        NamedTextColor color = buffer.readBoolean()
            ? NamedTextColor.NAMES.value(StringCodec.INSTANCE.decode(buffer)) : null;
        boolean friendlyFire = buffer.readBoolean();
        boolean seeFriendlyInvisibles = buffer.readBoolean();
        NameTagVisibility nameTagVisibility = NameTagVisibility.values()[buffer.readByte()];
        CollisionRule collisionRule = CollisionRule.values()[buffer.readByte()];
        return new TeamInfo(name, displayName, prefix, suffix, color, friendlyFire,
            seeFriendlyInvisibles, nameTagVisibility, collisionRule);
    }

}
