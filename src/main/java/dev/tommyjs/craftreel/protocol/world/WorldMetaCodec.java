package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.bukkit.World;
import org.bukkit.util.Vector;
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
            World.Environment.values()[buffer.readInt()],
            new Vector(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

}
