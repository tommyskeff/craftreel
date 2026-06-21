package dev.tommyjs.craftreel.protocol.chunk;

import org.jetbrains.annotations.NotNull;

public record ChunkSectionContent(char @NotNull [] section) {

    public static final int SECTION_SIZE = 16 * 16 * 16;

    public ChunkSectionContent {
        if (section.length != SECTION_SIZE) {
            throw new IllegalArgumentException("section must have " + SECTION_SIZE + " entries, got " + section.length);
        }
    }

}
