package dev.tommyjs.craftreel.protocol.chunk;

import com.google.common.base.Preconditions;
import dev.tommyjs.dynworld.region.CapturedRegion;
import org.jetbrains.annotations.NotNull;

public record ChunkSectionContent(@NotNull CapturedRegion region) {

    public ChunkSectionContent {
        Preconditions.checkArgument(region.getWidth() == 16);
        Preconditions.checkArgument(region.getHeight() == 16);
        Preconditions.checkArgument(region.getLength() == 16);
    }

}
