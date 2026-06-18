package dev.tommyjs.craftreel.protocol.scoreboard;

import dev.tommyjs.reel.track.codec.Codec;
import dev.tommyjs.reel.track.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectiveScoresCodec implements Codec<ObjectiveScores> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ObjectiveScores scores) {
        Map<String, Integer> entries = scores.scores();
        buffer.writeInt(entries.size());
        for (Map.Entry<String, Integer> entry : entries.entrySet()) {
            StringCodec.INSTANCE.encode(buffer, entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }

    @Override
    public @NotNull ObjectiveScores decode(@NotNull ByteBuf buffer) {
        int size = buffer.readInt();
        Map<String, Integer> entries = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = StringCodec.INSTANCE.decode(buffer);
            entries.put(key, buffer.readInt());
        }
        return new ObjectiveScores(entries);
    }

}
