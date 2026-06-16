package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.protocol.item.ItemStack;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityEquipmentStateCodec implements Codec<EntityEquipment> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityEquipment state) {
        Map<Integer, ItemStack> items = state.items();
        buffer.writeInt(items.size());
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            buffer.writeInt(entry.getKey());
            ItemStack.CODEC.encode(buffer, entry.getValue());
        }
    }

    @Override
    public @NotNull EntityEquipment decode(@NotNull ByteBuf buffer) {
        int count = buffer.readInt();
        Map<Integer, ItemStack> items = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            int slot = buffer.readInt();
            items.put(slot, ItemStack.CODEC.decode(buffer));
        }
        return new EntityEquipment(items);
    }

}
