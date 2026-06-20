package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.entity.EntityAnimationType;
import dev.tommyjs.craftreel.protocol.world.Environment;
import dev.tommyjs.craftreel.protocol.world.EnvironmentState;
import dev.tommyjs.craftreel.protocol.world.WorldMeta;
import dev.tommyjs.craftreel.record.entity.EntityCapture;
import dev.tommyjs.craftreel.record.entity.EntityCaptureRegistry;
import dev.tommyjs.craftreel.record.event.WorldRecordEvent;
import dev.tommyjs.craftreel.record.nms.WorldAccessDispatcher;
import dev.tommyjs.craftreel.record.nms.WorldAccessInjector;
import dev.tommyjs.craftreel.record.world.ChunkBounds;
import dev.tommyjs.craftreel.record.world.WorldBlockRecorder;
import dev.tommyjs.craftreel.record.world.WorldEffect;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.util.Vec3;
import dev.tommyjs.reel.recorder.EntityRecorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class WorldRecorder {

    private final MinecraftRecording recording;
    private final World world;
    private final ChunkBounds bounds;

    private final boolean active;
    private final Identifier identifier;
    private EntityRecorder worldEntity;
    private WorldEffect worldEffect;
    private WorldBlockRecorder blockRecorder;
    private EntityCaptureRegistry registry;
    private WorldAccessDispatcher dispatcher;
    private Listener bukkitListener;
    private EnvironmentState lastEnvironment;
    private boolean stopped;

    public static WorldRecorder attach(MinecraftRecording recording, Identifier identifier, World world) {
        return new WorldRecorder(recording, identifier, world, ChunkBounds.unbounded(), null);
    }

    public static WorldRecorder attach(MinecraftRecording recording, Identifier identifier, World world,
                                       @Nullable Vector spawn) {
        return new WorldRecorder(recording, identifier, world, ChunkBounds.unbounded(), spawn);
    }

    public static WorldRecorder attach(MinecraftRecording recording, Identifier identifier, World world, int minChunkX,
                                       int minChunkZ, int maxChunkX, int maxChunkZ) {
        return new WorldRecorder(recording, identifier, world,
            ChunkBounds.of(minChunkX, minChunkZ, maxChunkX, maxChunkZ), null);
    }

    public static WorldRecorder attach(MinecraftRecording recording, Identifier identifier, World world, int minChunkX,
                                       int minChunkZ, int maxChunkX, int maxChunkZ, @Nullable Vector spawn) {
        return new WorldRecorder(recording, identifier, world,
            ChunkBounds.of(minChunkX, minChunkZ, maxChunkX, maxChunkZ), spawn);
    }

    private WorldRecorder(MinecraftRecording recording, Identifier identifier, World world, ChunkBounds bounds,
                          @Nullable Vector spawn) {
        this.recording = recording;
        this.world = world;
        this.bounds = bounds;

        WorldRecordEvent event = new WorldRecordEvent(recording, world);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.active = false;
            this.identifier = null;
            return;
        }

        this.active = true;
        this.identifier = identifier;

        this.worldEntity = recording.getRecorder().createEntity(CraftReelProtocol.Entities.WORLD);
        Vector worldSpawn = spawn != null ? spawn : computeSpawn();
        worldEntity.recordState(CraftReelProtocol.Tracks.WORLD_META,
            new WorldMeta(identifier, Environment.valueOf(world.getEnvironment().name()),
                new Vec3(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ())));

        this.worldEffect = new WorldEffect(worldEntity);
        this.blockRecorder = new WorldBlockRecorder(recording, world, identifier, bounds);
        blockRecorder.captureLoadedChunks();
        this.registry = new EntityCaptureRegistry(recording, identifier, bounds);

        this.dispatcher = WorldAccessInjector.inject(world);
        dispatcher.addListener(worldEffect);
        dispatcher.addListener(blockRecorder);
        dispatcher.addListener(registry);

        for (Entity entity : world.getEntities()) {
            registry.add(entity);
        }

        this.bukkitListener = new Listener() {
            @EventHandler
            public void onWorldUnload(WorldUnloadEvent event) {
                if (event.getWorld() == world) {
                    stop();
                }
            }

            @EventHandler
            public void onExplosionPrime(ExplosionPrimeEvent event) {
                if (event.getEntity().getWorld() != world) {
                    return;
                }
                Location loc = event.getEntity().getLocation();
                worldEffect.recordExplosion(loc.getX(), loc.getY(), loc.getZ(), event.getRadius());
            }

            @EventHandler
            public void onAnimation(PlayerAnimationEvent event) {
                Player player = event.getPlayer();
                if (player.getWorld() != world) {
                    return;
                }
                EntityCapture capture = registry.byEntity(player);
                if (capture != null) {
                    capture.onAnimation(EntityAnimationType.SWING);
                }
            }

            @EventHandler
            public void onDamage(EntityDamageEvent event) {
                if (event.getEntity().getWorld() != world) {
                    return;
                }
                EntityCapture capture = registry.byEntity(event.getEntity());
                if (capture != null) {
                    capture.onAnimation(EntityAnimationType.HURT);
                }
            }

            @EventHandler
            public void onDeath(EntityDeathEvent event) {
                if (event.getEntity().getWorld() != world) {
                    return;
                }
                EntityCapture capture = registry.byEntity(event.getEntity());
                if (capture != null) {
                    capture.onAnimation(EntityAnimationType.DEATH);
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(bukkitListener, recording.getPlugin());

        recording.addTickListener(this::tick);
        recording.addStopListener(this::stop);
    }

    public void recordSound(String sound, double x, double y, double z, float volume, float pitch) {
        if (!active || stopped) {
            return;
        }
        worldEffect.onSound(sound, x, y, z, volume, pitch);
    }

    public void setBounds(int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        if (!active || stopped) {
            return;
        }
        if (!bounds.set(minChunkX, minChunkZ, maxChunkX, maxChunkZ)) {
            return;
        }
        blockRecorder.captureLoadedChunks();
        for (Entity entity : world.getEntities()) {
            registry.add(entity);
        }
    }

    public World getWorld() {
        return world;
    }

    public Identifier getContextId() {
        return identifier;
    }

    public boolean isActive() {
        return active;
    }

    private Vector computeSpawn() {
        if (!bounds.isBounded()) {
            Location spawn = world.getSpawnLocation();
            return new Vector(spawn.getX(), spawn.getY(), spawn.getZ());
        }

        int centerX = (((bounds.minChunkX() + bounds.maxChunkX()) >> 1) << 4) + 8;
        int centerZ = (((bounds.minChunkZ() + bounds.maxChunkZ()) >> 1) << 4) + 8;
        int y = world.getHighestBlockYAt(centerX, centerZ) + 1;
        return new Vector(centerX + 0.5, y, centerZ + 0.5);
    }

    private void tick() {
        if (!active || stopped) {
            return;
        }

        registry.tickAll();
        EnvironmentState environment = new EnvironmentState(identifier, world.getFullTime(), world.hasStorm(),
            world.isThundering());
        if (!environment.equals(lastEnvironment)) {
            worldEntity.recordState(CraftReelProtocol.Tracks.WORLD_ENVIRONMENT, environment);
            lastEnvironment = environment;
        }
    }

    public void stop() {
        if (!active || stopped) {
            return;
        }
        stopped = true;
        dispatcher.removeListener(worldEffect);
        dispatcher.removeListener(blockRecorder);
        dispatcher.removeListener(registry);
        HandlerList.unregisterAll(bukkitListener);
        registry.stopAll();
    }

}
