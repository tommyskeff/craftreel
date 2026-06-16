package dev.tommyjs.craftreel.protocol.item;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTLimiter;
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public record ItemStack(int id, int amount, int data, @Nullable NBTCompound nbt) {

    public static final ItemStack EMPTY = new ItemStack(0, 0, 0, null);

    public static final Codec<ItemStack> CODEC = new Codec<>() {
        @Override
        public void encode(@NotNull ByteBuf out, @NotNull ItemStack item) {
            out.writeInt(item.id());
            out.writeInt(item.amount());
            out.writeInt(item.data());
            writeNbt(out, item.nbt());
        }

        @Override
        public @NotNull ItemStack decode(@NotNull ByteBuf in) {
            return new ItemStack(in.readInt(), in.readInt(), in.readInt(), readNbt(in));
        }
    };

    public boolean isEmpty() {
        return amount <= 0;
    }

    private static void writeNbt(@NotNull ByteBuf out, @Nullable NBTCompound nbt) {
        if (nbt == null || nbt.isEmpty()) {
            out.writeBoolean(false);
            return;
        }

        out.writeBoolean(true);
        try (DataOutputStream stream = new DataOutputStream(new ByteBufOutputStream(out))) {
            DefaultNBTSerializer.INSTANCE.serializeTag(stream, nbt, false);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static @Nullable NBTCompound readNbt(@NotNull ByteBuf in) {
        if (!in.readBoolean()) {
            return null;
        }

        try (DataInputStream stream = new DataInputStream(new ByteBufInputStream(in))) {
            NBT nbt = DefaultNBTSerializer.INSTANCE.deserializeTag(NBTLimiter.noop(), stream, false);
            return nbt instanceof NBTCompound compound ? compound : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
