package dev.tommyjs.craftreel.protocol.team;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class TeamMembersCodec implements Codec<TeamMembers> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull TeamMembers members) {
        Set<String> entries = members.entries();
        buffer.writeInt(entries.size());
        for (String entry : entries) {
            StringCodec.INSTANCE.encode(buffer, entry);
        }
    }

    @Override
    public @NotNull TeamMembers decode(@NotNull ByteBuf buffer) {
        int size = buffer.readInt();
        Set<String> entries = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            entries.add(StringCodec.INSTANCE.decode(buffer));
        }
        return new TeamMembers(entries);
    }

}
