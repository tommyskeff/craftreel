package dev.tommyjs.craftreel.protocol.chunk;

import dev.tommyjs.craftreel.protocol.block.BlockState;
import org.jetbrains.annotations.NotNull;

public record ChunkSectionContent(char @NotNull [] section) {

    public static final int SECTION_SIZE = 16 * 16 * 16;

    public ChunkSectionContent {
        if (section.length != SECTION_SIZE) {
            throw new IllegalArgumentException("section must have " + SECTION_SIZE + " entries, got " + section.length);
        }
    }

    public static int index(int x, int y, int z) {
        return ((y & 0xF) << 8) | ((z & 0xF) << 4) | (x & 0xF);
    }

    public @NotNull BlockState blockAt(int x, int y, int z) {
        return BlockState.ofSectionId(section[index(x, y, z)]);
    }

}
