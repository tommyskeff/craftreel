package dev.tommyjs.craftreel.record.nms;

public interface WorldAccessListener {

    default void onBlockChange(int x, int y, int z) {
    }

    default void onSound(String sound, double x, double y, double z, float volume, float pitch) {
    }

    default void onParticle(int particleId, boolean longDistance, double x, double y, double z,
                            double offsetX, double offsetY, double offsetZ, int[] data) {
    }

    default void onEntityAdd(Object nmsEntity) {
    }

    default void onEntityRemove(Object nmsEntity) {
    }

    default void onWorldEvent(int type, int x, int y, int z, int data) {
    }

    default void onBlockBreak(int sourceEntityId, int x, int y, int z, int progress) {
    }

}
