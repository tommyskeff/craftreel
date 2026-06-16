package dev.tommyjs.craftreel.record.world;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.craftreel.record.nms.NmsAccess;
import dev.tommyjs.craftreel.record.nms.WorldAccessListener;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.dynworld.block.BlockState;
import dev.tommyjs.dynworld.region.CapturedRegion;
import dev.tommyjs.dynworld.world.DynamicWorld;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public final class WorldBlockRecorder implements WorldAccessListener {

    private final MinecraftRecording recording;
    private final World world;
    private final Identifier worldId;
    private final ChunkBounds bounds;
    private final Map<Long, WorldSection> sections = new HashMap<>();

    public WorldBlockRecorder(MinecraftRecording recording, World world, Identifier worldId, ChunkBounds bounds) {
        this.recording = recording;
        this.world = world;
        this.worldId = worldId;
        this.bounds = bounds;
    }

    public void captureLoadedChunks() {
        DynamicWorld dyn = DynamicWorld.fromBukkit(world);
        for (Chunk chunk : world.getLoadedChunks()) {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();

            if (!bounds.contains(chunkX, chunkZ)) {
                continue;
            }

            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            CapturedRegion area = CapturedRegion.capture(dyn, minX, 0, minZ, minX + 15, 255, minZ + 15, minX, 0, minZ);

            for (int sy = 0; sy < 16; sy++) {
                if (sections.containsKey(sectionKey(chunkX, sy, chunkZ))) {
                    continue;
                }

                if (area.getSectionNonAir(0, sy, 0) <= 0) {
                    continue;
                }

                char[] data = area.getSection(0, sy, 0);
                if (data == null) {
                    continue;
                }

                CapturedRegion mirror = new CapturedRegion(16, 16, 16, 0, 0, 0);
                mirror.setSection(0, 0, 0, data.clone());
                sections.put(sectionKey(chunkX, sy, chunkZ),
                    WorldSection.create(recording, worldId, chunkX, sy, chunkZ, mirror));
            }
        }
    }

    @Override
    public void onBlockChange(int x, int y, int z) {
        int sectionY = y >> 4;
        if (sectionY < 0 || sectionY > 15) {
            return;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (!bounds.contains(chunkX, chunkZ)) {
            return;
        }

        long key = sectionKey(chunkX, sectionY, chunkZ);
        WorldSection section = sections.get(key);
        if (section == null) {
            CapturedRegion mirror = new CapturedRegion(16, 16, 16, 0, 0, 0);
            mirror.setSection(0, 0, 0, new char[4096]);
            section = WorldSection.create(recording, worldId, chunkX, sectionY, chunkZ, mirror);
            sections.put(key, section);
        }

        BlockState after = NmsAccess.readBlockState(world, x, y, z);
        section.applyBlock(x & 15, y & 15, z & 15, after);
    }

    private static long sectionKey(int chunkX, int sectionY, int chunkZ) {
        return ((long) (chunkX & 0x3FFFFF) << 42) | ((long) (chunkZ & 0x3FFFFF) << 20) | (sectionY & 0xFF);
    }

}
