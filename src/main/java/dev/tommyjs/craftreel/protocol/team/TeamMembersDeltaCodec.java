package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class TeamMembersDeltaCodec implements Codec<TeamMembersDelta> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TeamMembersDelta delta) {
        if (delta instanceof TeamMembersDelta.AddEntries add) {
            buffer.writeByte(0);
            writeEntries(buffer, add.entries());
        } else if (delta instanceof TeamMembersDelta.RemoveEntries remove) {
            buffer.writeByte(1);
            writeEntries(buffer, remove.entries());
        } else {
            throw new IllegalStateException("Unknown team members delta: " + delta);
        }
    }

    @Override
    public @NotNull TeamMembersDelta decode(@NotNull ByteBuf buffer) {
        int type = buffer.readByte();
        return switch (type) {
            case 0 -> new TeamMembersDelta.AddEntries(readEntries(buffer));
            case 1 -> new TeamMembersDelta.RemoveEntries(readEntries(buffer));
            default -> throw new IllegalStateException("Unknown team members delta type: " + type);
        };
    }

    private static void writeEntries(@NotNull ByteBuf buffer, @NotNull Set<String> entries) {
        buffer.writeInt(entries.size());
        for (String entry : entries) {
            StringCodec.INSTANCE.encode(buffer, entry);
        }
    }

    private static @NotNull Set<String> readEntries(@NotNull ByteBuf buffer) {
        int size = buffer.readInt();
        Set<String> entries = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            entries.add(StringCodec.INSTANCE.decode(buffer));
        }
        return entries;
    }

}
