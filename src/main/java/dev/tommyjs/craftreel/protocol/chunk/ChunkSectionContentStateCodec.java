package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.dynworld.region.CapturedRegion;
import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChunkSectionContentStateCodec implements Codec<ChunkSectionContent> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChunkSectionContent state) {
        CapturedRegion region = state.region();

        char[] section = region.getSection(0, 0, 0);
        assert section != null && section.length == 4096;

        for (char value : section) {
            buffer.writeChar(value);
        }
    }

    @Override
    public @NotNull ChunkSectionContent decode(@NotNull ByteBuf buffer) {
        CapturedRegion region = CapturedRegion.empty(16, 16, 16, 0, 0, 0);

        char[] section = new char[4096];
        for (int i = 0; i < section.length; i++) {
            section[i] = buffer.readChar();
        }

        region.setSection(0, 0, 0, section);

        return new ChunkSectionContent(region);
    }

}
