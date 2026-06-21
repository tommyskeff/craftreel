package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class SpectatorProtectionHandler extends ReplayHandler {

    private BukkitTask enforceTask;

    @Override
    protected void onLoad(SceneHandlerContext ctx) {
        for (UUID id : getReplay().getViewers()) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                applyProtection(viewer);
            }
        }

        enforceTask = Bukkit.getScheduler().runTaskTimer(getReplay().getPlugin(), this::enforce, 0L, 20L);
    }

    @Override
    protected void onStop() {
        if (enforceTask != null) {
            enforceTask.cancel();
            enforceTask = null;
        }

        for (UUID id : getReplay().getViewers()) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                clearProtection(viewer);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() == getReplay()) {
            applyProtection(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveReplayEvent event) {
        if (event.getReplay() == getReplay() && event.getPlayer().isOnline()) {
            clearProtection(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isViewer(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && isViewer(player)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20F);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (isViewer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isViewer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (isViewer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.PHYSICAL) && isViewer(event.getPlayer())) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    private void enforce() {
        for (UUID id : getReplay().getViewers()) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                enforceProtection(viewer);
            }
        }
    }

    private void applyProtection(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setExhaustion(0F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
            Integer.MAX_VALUE, 0, false, false), true);
        enforceProtection(player);
    }

    private void enforceProtection(Player player) {
        player.setFireTicks(0);
    }

    private void clearProtection(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

}
