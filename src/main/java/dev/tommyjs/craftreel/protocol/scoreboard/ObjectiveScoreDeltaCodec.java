package dev.tommyjs.craftreel.protocol.scoreboard;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ObjectiveScoreDeltaCodec implements Codec<ObjectiveScoreDelta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ObjectiveScoreDelta delta) {
        if (delta instanceof ObjectiveScoreDelta.AddScore add) {
            buffer.writeByte(0);
            StringCodec.INSTANCE.encode(buffer, add.entry());
            buffer.writeInt(add.value());
        } else if (delta instanceof ObjectiveScoreDelta.UpdateScore update) {
            buffer.writeByte(1);
            StringCodec.INSTANCE.encode(buffer, update.entry());
            buffer.writeInt(update.before());
            buffer.writeInt(update.after());
        } else if (delta instanceof ObjectiveScoreDelta.RemoveScore remove) {
            buffer.writeByte(2);
            StringCodec.INSTANCE.encode(buffer, remove.entry());
            buffer.writeInt(remove.before());
        } else {
            throw new IllegalStateException("Unknown score delta: " + delta);
        }
    }

    @Override
    public @NotNull ObjectiveScoreDelta decode(@NotNull ByteBuf buffer) {
        int type = buffer.readByte();
        return switch (type) {
            case 0 -> new ObjectiveScoreDelta.AddScore(StringCodec.INSTANCE.decode(buffer), buffer.readInt());
            case 1 -> new ObjectiveScoreDelta.UpdateScore(StringCodec.INSTANCE.decode(buffer), buffer.readInt(), buffer.readInt());
            case 2 -> new ObjectiveScoreDelta.RemoveScore(StringCodec.INSTANCE.decode(buffer), buffer.readInt());
            default -> throw new IllegalStateException("Unknown score delta type: " + type);
        };
    }

}
