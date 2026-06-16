package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.protocol.item.ItemStack;
import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record EntityMetadataValue(@NotNull Type type, @NotNull Object value) {

    public enum Type {
        BYTE, SHORT, INT, FLOAT, STRING, BOOLEAN, ITEMSTACK
    }

    public static final Codec<EntityMetadataValue> CODEC = new Codec<>() {
        @Override
        public void encode(@NotNull ByteBuf out, @NotNull EntityMetadataValue value) {
            out.writeByte(value.type().ordinal());
            switch (value.type()) {
                case BYTE -> out.writeByte((Byte) value.value());
                case SHORT -> out.writeShort((Short) value.value());
                case INT -> out.writeInt((Integer) value.value());
                case FLOAT -> out.writeFloat((Float) value.value());
                case STRING -> StringCodec.INSTANCE.encode(out, (String) value.value());
                case BOOLEAN -> out.writeBoolean((Boolean) value.value());
                case ITEMSTACK -> ItemStack.CODEC.encode(out, (ItemStack) value.value());
            }
        }

        @Override
        public @NotNull EntityMetadataValue decode(@NotNull ByteBuf in) {
            Type type = Type.values()[in.readByte()];
            return switch (type) {
                case BYTE -> ofByte(in.readByte());
                case SHORT -> ofShort(in.readShort());
                case INT -> ofInt(in.readInt());
                case FLOAT -> ofFloat(in.readFloat());
                case STRING -> ofString(StringCodec.INSTANCE.decode(in));
                case BOOLEAN -> ofBoolean(in.readBoolean());
                case ITEMSTACK -> ofItem(ItemStack.CODEC.decode(in));
            };
        }
    };

    public static @NotNull EntityMetadataValue ofByte(int value) {
        return new EntityMetadataValue(Type.BYTE, (byte) value);
    }

    public static @NotNull EntityMetadataValue ofShort(int value) {
        return new EntityMetadataValue(Type.SHORT, (short) value);
    }

    public static @NotNull EntityMetadataValue ofInt(int value) {
        return new EntityMetadataValue(Type.INT, value);
    }

    public static @NotNull EntityMetadataValue ofFloat(float value) {
        return new EntityMetadataValue(Type.FLOAT, value);
    }

    public static @NotNull EntityMetadataValue ofString(@NotNull String value) {
        return new EntityMetadataValue(Type.STRING, value);
    }

    public static @NotNull EntityMetadataValue ofBoolean(boolean value) {
        return new EntityMetadataValue(Type.BOOLEAN, value);
    }

    public static @NotNull EntityMetadataValue ofItem(@NotNull ItemStack value) {
        return new EntityMetadataValue(Type.ITEMSTACK, value);
    }

}
