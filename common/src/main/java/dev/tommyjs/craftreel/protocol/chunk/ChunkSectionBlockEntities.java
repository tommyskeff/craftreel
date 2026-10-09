package dev.tommyjs.craftreel.protocol.chunk;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChunkSectionBlockEntities {

    private final Map<Integer, byte[]> entries;

    public ChunkSectionBlockEntities(@NotNull Map<Integer, byte[]> entries) {
        this.entries = new LinkedHashMap<>(entries);
    }

    public static int index(int x, int y, int z) {
        return ((y & 0xF) << 8) | ((z & 0xF) << 4) | (x & 0xF);
    }

    public @NotNull Map<Integer, byte[]> entries() {
        return entries;
    }

}
