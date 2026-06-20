package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.util.Vec3;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class WorldMetaCodec implements Codec<WorldMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull WorldMeta meta) {
        Identifier.CODEC.encode(buffer, meta.id());
        buffer.writeInt(meta.environment().ordinal());
        buffer.writeDouble(meta.spawnLocation().getX());
        buffer.writeDouble(meta.spawnLocation().getY());
        buffer.writeDouble(meta.spawnLocation().getZ());
    }

    @Override
    public @NotNull WorldMeta decode(@NotNull ByteBuf buffer) {
        return new WorldMeta(Identifier.CODEC.decode(buffer),
            Environment.values()[buffer.readInt()],
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

}
