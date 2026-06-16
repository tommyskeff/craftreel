package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.protocol.item.ItemStack;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class EntityEquipmentDeltaCodec implements Codec<EntityEquipmentDelta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityEquipmentDelta delta) {
        buffer.writeInt(delta.slot());
        ItemStack.CODEC.encode(buffer, delta.before());
        ItemStack.CODEC.encode(buffer, delta.after());
    }

    @Override
    public @NotNull EntityEquipmentDelta decode(@NotNull ByteBuf buffer) {
        return new EntityEquipmentDelta(buffer.readInt(), ItemStack.CODEC.decode(buffer), ItemStack.CODEC.decode(buffer));
    }

}
