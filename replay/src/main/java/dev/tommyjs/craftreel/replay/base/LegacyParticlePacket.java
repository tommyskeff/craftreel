package dev.tommyjs.craftreel.replay.base;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import dev.tommyjs.craftreel.protocol.world.ParticleEvent;
import org.jetbrains.annotations.NotNull;

public final class LegacyParticlePacket extends PacketWrapper<LegacyParticlePacket> {

    private final LegacyParticle particle;
    private final ParticleEvent event;

    public LegacyParticlePacket(@NotNull LegacyParticle particle, @NotNull ParticleEvent event) {
        super(PacketType.Play.Server.PARTICLE);
        this.particle = particle;
        this.event = event;
    }

    @Override
    public void write() {
        writeInt(particle.id());
        writeBoolean(true);
        writeFloat((float) event.x());
        writeFloat((float) event.y());
        writeFloat((float) event.z());
        writeFloat(event.offsetX());
        writeFloat(event.offsetY());
        writeFloat(event.offsetZ());
        writeFloat(event.speed());
        writeInt(event.count());
        for (int argument : particle.arguments(event.particle())) {
            writeVarInt(argument);
        }
    }

}
