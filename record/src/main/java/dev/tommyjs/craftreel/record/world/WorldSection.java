package dev.tommyjs.craftreel.record.world;

import dev.tommyjs.craftreel.record.util.BlockUtil;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContent;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentDelta;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionMeta;
import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.dynworld.block.BlockState;
import dev.tommyjs.dynworld.region.CapturedRegion;
import dev.tommyjs.reel.recorder.EntityRecorder;

public final class WorldSection {

    private final EntityRecorder recorder;
    private final CapturedRegion mirror;

    private WorldSection(EntityRecorder recorder, CapturedRegion mirror) {
        this.recorder = recorder;
        this.mirror = mirror;
    }

    public static WorldSection create(MinecraftRecording recording, Identifier worldId,
                                      int chunkX, int sectionY, int chunkZ, CapturedRegion mirror) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.CHUNK_SECTION);
        recorder.recordState(CraftReelProtocol.Tracks.CHUNK_SECTION_META,
            new ChunkSectionMeta(worldId, chunkX, sectionY, chunkZ));
        recorder.recordState(CraftReelProtocol.Tracks.CHUNK_SECTION_CONTENT,
            new ChunkSectionContent(mirror.getSection(0, 0, 0).clone()));
        return new WorldSection(recorder, mirror);
    }

    public void applyBlock(int localX, int localY, int localZ, BlockState after) {
        BlockState before = mirror.getBlock(localX, localY, localZ);
        if (before.equals(after)) {
            return;
        }
        recorder.recordDelta(CraftReelProtocol.Tracks.CHUNK_SECTION_CONTENT,
            new ChunkSectionContentDelta.BlockDelta(localX, localY, localZ, BlockUtil.adaptBlockState(before), BlockUtil.adaptBlockState(after)));
        mirror.setBlock(localX, localY, localZ, after);
    }

}
