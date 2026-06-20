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
        recorder.recordEvent(CraftReelProtocol.Tracks.PARTICLE,
            new ParticleEvent(NmsAccess.particleName(particleId), x, y, z,
                (float) offsetX, (float) offsetY, (float) offsetZ, 0f,
                data == null ? 1 : Math.max(1, data.length)));
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

}
