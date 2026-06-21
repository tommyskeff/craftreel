package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class SpectatorVoidHandler extends ReplayHandler {

    private final double minY;

    private BukkitTask task;

    public SpectatorVoidHandler() {
        this(0);
    }

    public SpectatorVoidHandler(double minY) {
        this.minY = minY;
    }

    @Override
    protected void onLoad(SceneHandlerContext ctx) {
        task = Bukkit.getScheduler().runTaskTimer(getReplay().getPlugin(), this::enforce, 0L, 1L);
    }

    @Override
    protected void onStop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void enforce() {
        for (UUID id : getReplay().getViewers()) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline() && viewer.getLocation().getY() < minY) {
                Location spawn = spawnFor(viewer);
                if (spawn != null) {
                    viewer.teleport(spawn);
                }
            }
        }
    }

    private Location spawnFor(Player viewer) {
        World world = viewer.getWorld();
        for (WorldContext context : getReplay().getScene().getResourceManager().getResources(BaseResources.WORLD)) {
            if (context.world().getBukkitWorld().equals(world)) {
                return context.spawn();
            }
        }
        return null;
    }

}
