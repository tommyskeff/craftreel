package dev.tommyjs.craftreel.protocol.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import dev.tommyjs.reel.track.codec.UuidCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityMetaCodec implements Codec<EntityMeta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityMeta meta) {
        UuidCodec.INSTANCE.encode(buffer, meta.id());
        Identifier.CODEC.encode(buffer, meta.worldId());
        StringCodec.INSTANCE.encode(buffer, meta.type().getName().toString());
        buffer.writeInt(meta.objectData());
    }

    @Override
    public @NotNull EntityMeta decode(@NotNull ByteBuf buffer) {
        UUID id = UuidCodec.INSTANCE.decode(buffer);
        Identifier worldId = Identifier.CODEC.decode(buffer);
        EntityType type = EntityTypes.getByName(StringCodec.INSTANCE.decode(buffer));
        int objectData = buffer.readInt();
        return new EntityMeta(id, worldId, type, objectData);
    }

}
