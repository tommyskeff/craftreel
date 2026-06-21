package dev.tommyjs.craftreel.replay.base.handler.controls;

import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReplayControlsHandler extends ReplayHandler {

    private final Map<UUID, Long> lastClick = new HashMap<>();
    private BukkitTask renderTask;
    private int skipIndex;

    @Override
    protected void onLoad(SceneHandlerContext ctx) {
        for (UUID id : getReplay().getViewers()) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                giveControls(viewer, true);
            }
        }
        renderTask = Bukkit.getScheduler().runTaskTimer(getReplay().getPlugin(), this::renderInfo, 0L, 10L);
    }

    @Override
    protected void onStop() {
        if (renderTask != null) {
            renderTask.cancel();
            renderTask = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() == getReplay()) {
            giveControls(event.getPlayer(), true);
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveReplayEvent event) {
        if (event.getReplay() == getReplay() && event.getPlayer().isOnline()) {
            clearControls(event.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isViewer(player) || !ReplayControls.isControl(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        if (onCooldown(player)) {
            return;
        }
        ReplayControl control = ReplayControls.fromSlot(player.getInventory().getHeldItemSlot());
        if (control != null && canUseControl(player, control)) {
            boolean leftClick = event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK;
            onControl(player, control, leftClick);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && isViewer(player)
            && ReplayControls.isControl(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    protected void onControl(Player player, ReplayControl control, boolean leftClick) {
        ScenePlayer scene = getReplay().getScenePlayer();
        switch (control) {
            case PLAY_PAUSE -> {
                scene.toggle();
                player.sendMessage(ChatColor.GRAY + (scene.isPlaying() ? "Playing." : "Paused."));
                playSound(player, Sound.CLICK, 1F);
            }
            case FORWARD -> {
                if (leftClick) {
                    cycleSkipInterval(player);
                    return;
                }
                long frame = Math.min(scene.getTotalFrames() - 1, scene.getCurrentFrame() + skipFrames());
                scene.seek(frame);
                player.sendMessage(ChatColor.GRAY + "Jumped to frame " + frame + ".");
                playSound(player, Sound.CLICK, 2F);
            }
            case REWIND -> {
                if (leftClick) {
                    cycleSkipInterval(player);
                    return;
                }
                long frame = Math.max(0, scene.getCurrentFrame() - skipFrames());
                scene.seek(frame);
                player.sendMessage(ChatColor.GRAY + "Jumped to frame " + frame + ".");
                playSound(player, Sound.CLICK, 2F);
            }
            case SLOWER -> {
                if (scene.getSpeed() <= 0.25) {
                    playSound(player, Sound.ENDERMAN_TELEPORT, 0.5F);
                    return;
                }
                scene.setSpeed(scene.getSpeed() / 2.0);
                player.sendMessage(ChatColor.GRAY + "Speed: " + ReplayControls.speedLabel(scene.getSpeed()));
                playSound(player, Sound.ENDERMAN_IDLE, 2F);
            }
            case FASTER -> {
                if (scene.getSpeed() >= 8.0) {
                    playSound(player, Sound.ENDERMAN_TELEPORT, 0.5F);
                    return;
                }
                scene.setSpeed(scene.getSpeed() * 2.0);
                player.sendMessage(ChatColor.GRAY + "Speed: " + ReplayControls.speedLabel(scene.getSpeed()));
                playSound(player, Sound.ENDERMAN_IDLE, 2F);
            }
        }
        giveControls(player, false);
        renderInfo(player);
    }

    protected long cooldownMillis() {
        return 250L;
    }

    private boolean onCooldown(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastClick.get(player.getUniqueId());
        if (last != null && now - last < cooldownMillis()) {
            return true;
        }
        lastClick.put(player.getUniqueId(), now);
        return false;
    }

    protected long skipFrames() {
        return ReplayControls.SKIP_INTERVALS[skipIndex];
    }

    protected void cycleSkipInterval(Player player) {
        skipIndex = (skipIndex + 1) % ReplayControls.SKIP_INTERVALS.length;
        giveControls(player, false);
        playSound(player, Sound.CLICK, 1.5F);
    }

    public void giveControls(Player player, boolean focus) {
        ScenePlayer scene = getReplay().getScenePlayer();
        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < 9; slot++) {
            ReplayControl control = ReplayControls.fromSlot(slot);
            inv.setItem(slot, control == null ? null : getControlItem(player, control, scene));
        }
        if (focus) {
            inv.setHeldItemSlot(ReplayControls.HELD_SLOT);
        }
        player.setLevel(0);
        player.updateInventory();
    }

    protected ItemStack getControlItem(Player player, ReplayControl control, ScenePlayer scene) {
        double speed = scene.getSpeed();
        long interval = skipFrames();
        return switch (control) {
            case SLOWER -> ReplayControls.slower(speed);
            case REWIND -> ReplayControls.rewind(interval);
            case PLAY_PAUSE -> ReplayControls.playPause(!scene.isPlaying());
            case FORWARD -> ReplayControls.forward(interval);
            case FASTER -> ReplayControls.faster(speed);
        };
    }

    protected boolean canUseControl(Player player, ReplayControl control) {
        return true;
    }

    public void clearControls(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ReplayControl control : ReplayControl.values()) {
            inv.setItem(control.slot(), null);
        }
        player.setExp(0F);
        player.updateInventory();
    }

    private void renderInfo() {
        for (UUID id : getReplay().getViewers()) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                renderInfo(viewer);
            }
        }
    }

    protected void renderInfo(Player player) {
        ScenePlayer scene = getReplay().getScenePlayer();

        String status = scene.isPlaying()
            ? ChatColor.GREEN + "Playing"
            : ChatColor.RED + (scene.getProgress() >= 1.0 ? "Finished" : "Paused");

        String info = status
            + "    "
            + ChatColor.YELLOW + formatTime(scene.getCurrentTime())
            + " / "
            + formatTime(scene.getTotalTime())
            + "    "
            + ChatColor.GOLD + ReplayControls.speedLabel(scene.getSpeed());

        WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(
            new ChatMessageLegacy(Component.text(info), ChatTypes.GAME_INFO));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        player.setExp(clampProgress(scene.getProgress()));
    }

    private static String formatTime(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static float clampProgress(double progress) {
        return (float) Math.max(0.0, Math.min(1.0, progress));
    }

    protected void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 1F, pitch);
    }

}
