package dev.tommyjs.craftreel.record.world;

import dev.tommyjs.craftreel.record.util.BlockUtil;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionBlockEntities;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionBlockEntityDelta;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContent;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentDelta;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionMeta;
import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.dynworld.block.BlockState;
import dev.tommyjs.dynworld.region.CapturedRegion;
import dev.tommyjs.reel.recorder.EntityRecorder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class WorldSection {

    private final EntityRecorder recorder;
    private final CapturedRegion mirror;
    private final Map<Integer, byte[]> blockEntities;

    private WorldSection(EntityRecorder recorder, CapturedRegion mirror, Map<Integer, byte[]> blockEntities) {
        this.recorder = recorder;
        this.mirror = mirror;
        this.blockEntities = blockEntities;
    }

    public static WorldSection create(MinecraftRecording recording, Identifier worldId,
                                      int chunkX, int sectionY, int chunkZ, CapturedRegion mirror,
                                      Map<Integer, byte[]> blockEntities) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.CHUNK_SECTION);
        recorder.recordState(CraftReelProtocol.Tracks.CHUNK_SECTION_META,
            new ChunkSectionMeta(worldId, chunkX, sectionY, chunkZ));
        recorder.recordState(CraftReelProtocol.Tracks.CHUNK_SECTION_CONTENT,
            new ChunkSectionContent(mirror.getSection(0, 0, 0).clone()));
        recorder.recordState(CraftReelProtocol.Tracks.CHUNK_SECTION_BLOCK_ENTITIES,
            new ChunkSectionBlockEntities(blockEntities));
        return new WorldSection(recorder, mirror, new HashMap<>(blockEntities));
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

    public void applyBlockEntity(int localX, int localY, int localZ, byte @Nullable [] after) {
        int index = ChunkSectionBlockEntities.index(localX, localY, localZ);
        byte[] before = blockEntities.get(index);
        if (Arrays.equals(before, after)) {
            return;
        }
        recorder.recordDelta(CraftReelProtocol.Tracks.CHUNK_SECTION_BLOCK_ENTITIES,
            new ChunkSectionBlockEntityDelta(index, before, after));
        if (after == null) {
            blockEntities.remove(index);
        } else {
            blockEntities.put(index, after);
        }
    }

}
