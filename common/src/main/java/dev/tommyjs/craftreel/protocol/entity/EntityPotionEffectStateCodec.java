package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EntityPotionEffectStateCodec implements Codec<EntityPotionEffectState> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull EntityPotionEffectState state) {
        List<EntityPotionEffect> effects = state.effects();
        buffer.writeInt(effects.size());
        for (EntityPotionEffect effect : effects) {
            buffer.writeInt(effect.typeId());
            buffer.writeInt(effect.amplifier());
            buffer.writeInt(effect.duration());
            buffer.writeBoolean(effect.ambient());
            buffer.writeBoolean(effect.particles());
        }
    }

    @Override
    public @NotNull EntityPotionEffectState decode(@NotNull ByteBuf buffer) {
        int count = buffer.readInt();
        List<EntityPotionEffect> effects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            effects.add(new EntityPotionEffect(buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readBoolean(), buffer.readBoolean()));
        }
        return new EntityPotionEffectState(effects);
    }

}
