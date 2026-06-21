package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.dynworld.block.BlockState;
import dev.tommyjs.dynworld.region.CapturedRegion;
import dev.tommyjs.dynworld.region.PasteOptions;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentDelta;

import java.util.List;

public final class ChunkSectionActor extends AbstractActor {

    private static final int WHOLE_SUBCHUNK_THRESHOLD = 64;

    private WorldContext owner;
    private int originX, originY, originZ;

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.CHUNK_SECTION_META, meta -> {
            owner = scene.getResourceManager().require(BaseResources.WORLD, meta.worldId());
            originX = meta.x() << 4;
            originY = meta.y() << 4;
            originZ = meta.z() << 4;
        });

        onChange(CraftReelProtocol.Tracks.CHUNK_SECTION_CONTENT, batch -> {
            int blocks = batch.reset() ? Integer.MAX_VALUE : countBlocks(batch.deltas());
            if (blocks >= WHOLE_SUBCHUNK_THRESHOLD) {
                CapturedRegion region = CapturedRegion.empty(16, 16, 16, 0, 0, 0);
                region.setSection(0, 0, 0, batch.state().section());
                owner.world().setRegion(region, originX, originY, originZ, PasteOptions.create());
            } else {
                for (ChunkSectionContentDelta delta : batch.deltas()) {
                    applyDelta(delta);
                }
            }
        });

        onDestroy(() -> {
            if (owner != null && owner.world().isActive()) {
                owner.world().setRegion(CapturedRegion.empty(16, 16, 16, 0, 0, 0),
                    originX, originY, originZ, PasteOptions.create());
            }
        });
    }

    private static int countBlocks(List<ChunkSectionContentDelta> deltas) {
        int count = 0;
        for (ChunkSectionContentDelta delta : deltas) {
            if (delta instanceof ChunkSectionContentDelta.MultiBlockDelta multi) {
                count += multi.blocks().length;
            } else {
                count += 1;
            }
        }
        return count;
    }

    private void applyDelta(ChunkSectionContentDelta delta) {
        if (delta instanceof ChunkSectionContentDelta.BlockDelta block) {
            setBlock(block);
        } else if (delta instanceof ChunkSectionContentDelta.MultiBlockDelta multi) {
            for (ChunkSectionContentDelta.BlockDelta block : multi.blocks()) {
                setBlock(block);
            }
        } else {
            throw new IllegalStateException("Unknown delta: " + delta);
        }
    }

    private void setBlock(ChunkSectionContentDelta.BlockDelta block) {
        BlockState state = BlockState.of(block.after().id(), block.after().data());
        owner.world().setBlock(originX + block.x(), originY + block.y(), originZ + block.z(), state);
    }

}
