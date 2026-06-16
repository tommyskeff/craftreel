package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.protocol.block.BlockBreakProgress;
import dev.tommyjs.craftreel.protocol.world.ExplosionEvent;
import dev.tommyjs.craftreel.protocol.world.ParticleEvent;
import dev.tommyjs.craftreel.protocol.world.SoundEvent;
import dev.tommyjs.craftreel.protocol.world.WorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class EffectsActor extends AbstractActor implements Viewable {

    private static final double SOUND_RANGE = 48;
    private static final double PARTICLE_RANGE = 48;
    private static final double EXPLOSION_RANGE = 80;

    private Identifier id;
    private World world;
    private WorldActor owner;
    private final Set<UUID> viewers = new LinkedHashSet<>();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.WORLD_META, meta -> {
            id = meta.id();
            WorldContext context = scene.getResourceManager().require(BaseResources.WORLD, id);
            owner = (WorldActor) context;
            world = context.world().getBukkitWorld();
            scene.getResourceManager().publish(BaseResources.EFFECTS, id, this);
            owner.addViewable(this);
        });

        onEvent(CraftReelProtocol.Tracks.SOUND, this::playSound);
        onEvent(CraftReelProtocol.Tracks.PARTICLE, this::playParticle);
        onEvent(CraftReelProtocol.Tracks.EXPLOSION, this::playExplosion);
        onEvent(CraftReelProtocol.Tracks.WORLD_EVENT, this::playWorldEvent);
        onEvent(CraftReelProtocol.Tracks.BLOCK_BREAK, this::playBlockBreak);

        onDestroy(() -> {
            if (id != null) {
                scene.getResourceManager().unpublish(BaseResources.EFFECTS, id);
            }
            if (owner != null) {
                owner.removeViewable(this);
            }
            viewers.clear();
        });
    }

    @Override
    public void addViewer(@NotNull Player player) {
        viewers.add(player.getUniqueId());
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        viewers.remove(player.getUniqueId());
    }

    private void playSound(SoundEvent event) {
        Location location = new Location(world, event.x(), event.y(), event.z());
        Sound sound = resolveSound(event.sound());
        if (sound != null) {
            forViewersInRange(location, SOUND_RANGE, viewer ->
                viewer.playSound(location, sound, event.volume(), event.pitch()));
            return;
        }

        PacketWrapper<?> packet;
        try {
            packet = new WrapperPlayServerSoundEffect(Sounds.getByNameOrCreate(event.sound()),
                SoundCategory.MASTER, new Vector3d(event.x(), event.y(), event.z()),
                event.volume(), event.pitch());
        } catch (RuntimeException e) {
            return;
        }
        forViewersInRange(location, SOUND_RANGE, viewer -> send(viewer, packet));
    }

    private void playParticle(ParticleEvent event) {
        ParticleType type = ParticleTypes.getByName(event.particle());
        Location location = new Location(world, event.x(), event.y(), event.z());
        PacketWrapper<?> packet = new WrapperPlayServerParticle(new Particle(type), true,
            new Vector3d(event.x(), event.y(), event.z()),
            new Vector3f(event.offsetX(), event.offsetY(), event.offsetZ()), event.speed(), event.count());
        forViewersInRange(location, PARTICLE_RANGE, viewer -> send(viewer, packet));
    }

    private void playExplosion(ExplosionEvent event) {
        Location location = new Location(world, event.x(), event.y(), event.z());
        PacketWrapper<?> packet = new WrapperPlayServerExplosion(new Vector3d(event.x(), event.y(), event.z()),
            event.strength(), Collections.emptyList(), new Vector3f(0, 0, 0));
        forViewersInRange(location, EXPLOSION_RANGE, viewer -> send(viewer, packet));
    }

    private void playWorldEvent(WorldEvent event) {
        Location location = new Location(world, event.x(), event.y(), event.z());
        PacketWrapper<?> packet = new WrapperPlayServerEffect(event.type(),
            new Vector3i(event.x(), event.y(), event.z()), event.data(), false);
        forViewersInRange(location, PARTICLE_RANGE, viewer -> send(viewer, packet));
    }

    private void playBlockBreak(BlockBreakProgress event) {
        Location location = new Location(world, event.x(), event.y(), event.z());
        PacketWrapper<?> packet = new WrapperPlayServerBlockBreakAnimation(event.sourceEntityId(),
            new Vector3i(event.x(), event.y(), event.z()), (byte) event.progress());
        forViewersInRange(location, PARTICLE_RANGE, viewer -> send(viewer, packet));
    }

    private void forViewersInRange(Location location, double range, Consumer<Player> action) {
        double rangeSquared = range * range;
        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer == null || !viewer.isOnline() || !viewer.getWorld().equals(world)) {
                continue;
            }
            if (viewer.getLocation().distanceSquared(location) <= rangeSquared) {
                action.accept(viewer);
            }
        }
    }

    private static Sound resolveSound(String name) {
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void send(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}
