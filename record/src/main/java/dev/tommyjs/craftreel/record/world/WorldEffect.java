package dev.tommyjs.craftreel.record.world;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.world.ExplosionEvent;
import dev.tommyjs.craftreel.protocol.world.ParticleEvent;
import dev.tommyjs.craftreel.protocol.world.SoundEvent;
import dev.tommyjs.craftreel.protocol.block.BlockBreakProgress;
import dev.tommyjs.craftreel.protocol.world.WorldEvent;
import dev.tommyjs.craftreel.record.nms.NmsAccess;
import dev.tommyjs.craftreel.record.nms.WorldAccessListener;
import dev.tommyjs.reel.recorder.EntityRecorder;

public final class WorldEffect implements WorldAccessListener {

    private final EntityRecorder recorder;

    public WorldEffect(EntityRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void onSound(String sound, double x, double y, double z, float volume, float pitch) {
        recorder.recordEvent(CraftReelProtocol.Tracks.SOUND, new SoundEvent(sound, x, y, z, volume, pitch));
    }

    @Override
    public void onParticle(int particleId, boolean longDistance, double x, double y, double z,
                           double offsetX, double offsetY, double offsetZ, int[] data) {
        recordParticle(particleName(NmsAccess.particleName(particleId), data), x, y, z,
            (float) offsetX, (float) offsetY, (float) offsetZ, 1f, 0);
    }

    public void recordParticle(String particle, double x, double y, double z,
                               float offsetX, float offsetY, float offsetZ, float speed, int count) {
        recorder.recordEvent(CraftReelProtocol.Tracks.PARTICLE,
            new ParticleEvent(particle, x, y, z, offsetX, offsetY, offsetZ, speed, count));
    }

    @Override
    public void onWorldEvent(int type, int x, int y, int z, int data) {
        recorder.recordEvent(CraftReelProtocol.Tracks.WORLD_EVENT, new WorldEvent(type, x, y, z, data));
    }

    @Override
    public void onBlockBreak(int sourceEntityId, int x, int y, int z, int progress) {
        recorder.recordEvent(CraftReelProtocol.Tracks.BLOCK_BREAK,
            new BlockBreakProgress(sourceEntityId, x, y, z, progress));
    }

    public void recordExplosion(double x, double y, double z, float strength) {
        recorder.recordEvent(CraftReelProtocol.Tracks.EXPLOSION, new ExplosionEvent(x, y, z, strength));
    }

    private static String particleName(String name, int[] data) {
        if (data == null || data.length == 0 || !name.endsWith("_")) {
            return name;
        }
        StringBuilder builder = new StringBuilder(name.substring(0, name.length() - 1));
        for (int value : data) {
            builder.append('_').append(value);
        }
        return builder.toString();
    }

}
