package dev.tommyjs.craftreel.record.world;

import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionBlockEntities;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class WorldBlockRecorder implements WorldAccessListener {

    private final MinecraftRecording recording;
    private final World world;
    private final Identifier worldId;
    private final ChunkBounds bounds;
    private final Map<Long, WorldSection> sections = new HashMap<>();
    private final Set<Long> pendingBlockEntities = new LinkedHashSet<>();

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
            Map<Integer, Map<Integer, byte[]>> blockEntities = captureBlockEntities(chunk);

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
                sections.put(sectionKey(chunkX, sy, chunkZ), WorldSection.create(recording, worldId, chunkX, sy,
                    chunkZ, mirror, blockEntities.getOrDefault(sy, Map.of())));
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
            section = WorldSection.create(recording, worldId, chunkX, sectionY, chunkZ, mirror, Map.of());
            sections.put(key, section);
        }

        BlockState after = NmsAccess.readBlockState(world, x, y, z);
        section.applyBlock(x & 15, y & 15, z & 15, after);
        pendingBlockEntities.add(blockKey(x, y, z));
    }

    public void tick() {
        if (pendingBlockEntities.isEmpty()) {
            return;
        }

        for (long key : pendingBlockEntities) {
            int x = (int) (key >> 38);
            int y = (int) (key << 26 >> 52);
            int z = (int) (key << 38 >> 38);
            WorldSection section = sections.get(sectionKey(x >> 4, y >> 4, z >> 4));
            if (section == null || !world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            section.applyBlockEntity(x & 15, y & 15, z & 15, NmsAccess.readBlockEntity(world, x, y, z));
        }
        pendingBlockEntities.clear();
    }

    private Map<Integer, Map<Integer, byte[]>> captureBlockEntities(Chunk chunk) {
        Map<Integer, Map<Integer, byte[]>> bySection = new HashMap<>();
        for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
            int x = state.getX();
            int y = state.getY();
            int z = state.getZ();
            byte[] nbt = NmsAccess.readBlockEntity(world, x, y, z);
            if (nbt != null) {
                bySection.computeIfAbsent(y >> 4, ignored -> new HashMap<>())
                    .put(ChunkSectionBlockEntities.index(x, y, z), nbt);
            }
        }
        return bySection;
    }

    private static long sectionKey(int chunkX, int sectionY, int chunkZ) {
        return ((long) (chunkX & 0x3FFFFF) << 42) | ((long) (chunkZ & 0x3FFFFF) << 20) | (sectionY & 0xFF);
    }

    private static long blockKey(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (y & 0xFFF) << 26) | (z & 0x3FFFFFF);
    }

}
