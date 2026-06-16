package dev.tommyjs.craftreel.record.world;

/**
 * Mutable chunk-range filter shared between a world recorder and its block/entity recorders.
 * Updating the bounds here is reflected everywhere that holds the same instance.
 */
public final class ChunkBounds {

    private boolean bounded;
    private int minChunkX, minChunkZ, maxChunkX, maxChunkZ;

    private ChunkBounds(boolean bounded, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        this.bounded = bounded;
        this.minChunkX = Math.min(minChunkX, maxChunkX);
        this.maxChunkX = Math.max(minChunkX, maxChunkX);
        this.minChunkZ = Math.min(minChunkZ, maxChunkZ);
        this.maxChunkZ = Math.max(minChunkZ, maxChunkZ);
    }

    public static ChunkBounds unbounded() {
        return new ChunkBounds(false, 0, 0, 0, 0);
    }

    public static ChunkBounds of(int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        return new ChunkBounds(true, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
    }

    /**
     * Updates the bounds to the given chunk range. Returns {@code true} if the range actually changed.
     */
    public boolean set(int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        int newMinX = Math.min(minChunkX, maxChunkX);
        int newMaxX = Math.max(minChunkX, maxChunkX);
        int newMinZ = Math.min(minChunkZ, maxChunkZ);
        int newMaxZ = Math.max(minChunkZ, maxChunkZ);
        if (bounded && newMinX == this.minChunkX && newMaxX == this.maxChunkX
            && newMinZ == this.minChunkZ && newMaxZ == this.maxChunkZ) {
            return false;
        }
        this.bounded = true;
        this.minChunkX = newMinX;
        this.maxChunkX = newMaxX;
        this.minChunkZ = newMinZ;
        this.maxChunkZ = newMaxZ;
        return true;
    }

    public boolean isBounded() {
        return bounded;
    }

    public boolean contains(int chunkX, int chunkZ) {
        if (!bounded) {
            return true;
        }
        return chunkX >= minChunkX && chunkX <= maxChunkX && chunkZ >= minChunkZ && chunkZ <= maxChunkZ;
    }

    public int minChunkX() {
        return minChunkX;
    }

    public int minChunkZ() {
        return minChunkZ;
    }

    public int maxChunkX() {
        return maxChunkX;
    }

    public int maxChunkZ() {
        return maxChunkZ;
    }

}
