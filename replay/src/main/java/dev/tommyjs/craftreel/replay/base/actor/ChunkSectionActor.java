package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.dynworld.block.BlockState;
import dev.tommyjs.dynworld.region.CapturedRegion;
import dev.tommyjs.dynworld.region.PasteOptions;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.base.BlockEntityAccess;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionBlockEntityDelta;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentDelta;
import org.bukkit.World;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ChunkSectionActor extends AbstractActor {

    private static final int WHOLE_SUBCHUNK_THRESHOLD = 64;

    private final Set<Integer> blockEntities = new HashSet<>();

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

        onChange(CraftReelProtocol.Tracks.CHUNK_SECTION_BLOCK_ENTITIES, batch -> {
            Set<Integer> changed = new LinkedHashSet<>();
            if (batch.reset()) {
                changed.addAll(blockEntities);
                changed.addAll(batch.state().entries().keySet());
            } else {
                for (ChunkSectionBlockEntityDelta delta : batch.deltas()) {
                    changed.add(delta.index());
                }
            }

            World world = owner.world().getBukkitWorld();
            for (int index : changed) {
                int x = originX + (index & 15);
                int y = originY + ((index >> 8) & 15);
                int z = originZ + ((index >> 4) & 15);
                byte[] nbt = batch.state().entries().get(index);
                if (nbt == null) {
                    BlockEntityAccess.remove(world, x, y, z);
                    blockEntities.remove(index);
                } else {
                    BlockEntityAccess.load(world, x, y, z, nbt);
                    blockEntities.add(index);
                }
            }
        });

        onDestroy(() -> {
            if (owner != null && owner.world().isActive()) {
                World world = owner.world().getBukkitWorld();
                for (int index : blockEntities) {
                    BlockEntityAccess.remove(world, originX + (index & 15), originY + ((index >> 8) & 15),
                        originZ + ((index >> 4) & 15));
                }
                blockEntities.clear();
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
