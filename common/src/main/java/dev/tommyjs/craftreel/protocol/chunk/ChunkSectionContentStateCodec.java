package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.reel.track.codec.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChunkSectionContentStateCodec implements Codec<ChunkSectionContent> {

    @Override
    public void encode(@NotNull ByteBuf buffer, @NotNull ChunkSectionContent state) {
        for (char value : state.section()) {
            buffer.writeChar(value);
        }
    }

    @Override
    public @NotNull ChunkSectionContent decode(@NotNull ByteBuf buffer) {
        char[] section = new char[ChunkSectionContent.SECTION_SIZE];
        for (int i = 0; i < section.length; i++) {
            section[i] = buffer.readChar();
        }

        return new ChunkSectionContent(section);
    }

}
