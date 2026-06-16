package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.dynworld.entity.EntityTracker;
import dev.tommyjs.dynworld.world.DynamicWorld;
import dev.tommyjs.dynworld.world.DynamicWorldManager;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class WorldActor extends AbstractActor implements WorldContext {

    private Identifier id;
    private DynamicWorld world;
    private Location spawn;

    private final EntityTracker tracker = EntityTracker.create();
    private final Set<UUID> viewers = new LinkedHashSet<>();
    private final List<Viewable> viewables = new ArrayList<>();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.WORLD_META, meta -> {
            id = meta.id();
            world = DynamicWorld.builder()
                .setName("replay-" + UUID.randomUUID())
                .setEnvironment(meta.environment())
                .setGameMode(GameMode.CREATIVE)
                .create();
            spawn = new Location(world.getBukkitWorld(),
                meta.spawnLocation().getX(), meta.spawnLocation().getY(), meta.spawnLocation().getZ());

            World bukkit = world.getBukkitWorld();
            bukkit.setGameRuleValue("doDaylightCycle", "false");
            bukkit.setGameRuleValue("doWeatherCycle", "false");

            scene.getResourceManager().publish(BaseResources.WORLD, id, this);
        });

        onState(CraftReelProtocol.Tracks.WORLD_ENVIRONMENT, true, environment -> {
            World bukkit = world.getBukkitWorld();
            bukkit.setFullTime(environment.time());
            bukkit.setStorm(environment.storm());
            bukkit.setThundering(environment.thundering());
        });

        onDestroy(() -> {
            for (SpectatorRegistry registry : scene.getResourceManager().getResources(BaseResources.SPECTATORS)) {
                registry.purge(this);
            }
            scene.getResourceManager().unpublish(BaseResources.WORLD, id);
            World bukkit = world.getBukkitWorld();
            if (!bukkit.getPlayers().isEmpty()) {
                Location fallback = Bukkit.getWorlds().get(0).getSpawnLocation();
                for (Player player : new ArrayList<>(bukkit.getPlayers())) {
                    player.teleport(fallback);
                }
            }
            DynamicWorldManager.DEFAULT.unload(world);
        });
    }

    @Override
    public @NotNull Identifier id() {
        return id;
    }

    @Override
    public @NotNull DynamicWorld world() {
        return world;
    }

    @Override
    public @NotNull EntityTracker tracker() {
        return tracker;
    }

    @Override
    public void attach(@NotNull Player player) {
        player.teleport(spawn);
        viewers.add(player.getUniqueId());
        tracker.addViewer(player);
        for (Viewable viewable : viewables) {
            viewable.addViewer(player);
        }
    }

    @Override
    public void detach(@NotNull Player player) {
        viewers.remove(player.getUniqueId());
        tracker.removeViewer(player);
        for (Viewable viewable : viewables) {
            viewable.removeViewer(player);
        }
    }

    public void addViewable(@NotNull Viewable viewable) {
        viewables.add(viewable);
        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                viewable.addViewer(viewer);
            }
        }
    }

    public void removeViewable(@NotNull Viewable viewable) {
        viewables.remove(viewable);
    }

}
