package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.dynworld.entity.EntityTracker;
import dev.tommyjs.dynworld.world.DynamicWorld;
import dev.tommyjs.dynworld.world.DynamicWorldManager;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.base.ViewerContexts;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.ViewerGroup;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public final class WorldActor extends AbstractActor implements WorldContext {

    private Identifier id;
    private DynamicWorld world;
    private Location spawn;

    private final EntityTracker tracker = EntityTracker.create();
    private final ViewerGroup group = new ViewerGroup();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.WORLD_META, meta -> {
            id = meta.id();
            world = DynamicWorld.builder()
                .setName("replay-" + UUID.randomUUID())
                .setEnvironment(World.Environment.valueOf(meta.environment().name()))
                .setGameMode(GameMode.CREATIVE)
                .create();
            spawn = new Location(world.getBukkitWorld(),
                meta.spawnLocation().getX(), meta.spawnLocation().getY(), meta.spawnLocation().getZ());

            World bukkit = world.getBukkitWorld();
            bukkit.setGameRuleValue("doDaylightCycle", "false");
            bukkit.setGameRuleValue("doWeatherCycle", "false");

            group.add(new Viewable() {
                @Override
                public void addViewer(@NotNull Player player) {
                    tracker.addViewer(player);
                }

                @Override
                public void removeViewer(@NotNull Player player) {
                    tracker.removeViewer(player);
                }
            });

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
            group.clear();
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
    public @NotNull Location spawn() {
        return spawn;
    }

    @Override
    public @NotNull ViewerGroup group() {
        return group;
    }

    @Override
    public void addViewer(@NotNull Player player) {
        ViewerContexts.makeExclusive(scene.getResourceManager(), BaseResources.WORLD, this, player);
        player.teleport(spawn);
        group.addViewer(player);
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        group.removeViewer(player);
    }

}
