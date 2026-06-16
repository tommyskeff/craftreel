package dev.tommyjs.craftreelexample;

import dev.tommyjs.craftreel.CraftReel;
import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.craftreel.record.SidebarRecorder;
import dev.tommyjs.craftreel.record.TextRecorder;
import dev.tommyjs.craftreel.record.WorldRecorder;
import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.util.Identifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import dev.tommyjs.reel.storage.writer.LayerStrategy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CraftReelExamplePlugin extends JavaPlugin implements Listener {

    private final Map<UUID, MinecraftRecording> recordings = new HashMap<>();
    private final Map<UUID, MinecraftReplay> replays = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getDataFolder().mkdirs();
    }

    @Override
    public void onDisable() {
        for (MinecraftRecording recording : recordings.values()) {
            recording.stop();
        }

        recordings.clear();
        for (MinecraftReplay replay : replays.values()) {
            replay.close();
        }

        replays.clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be run by a player.");
            return true;
        }

        String name = args.length > 0 ? args[0] : "demo";
        return switch (command.getName().toLowerCase()) {
            case "reelrecord" -> handleRecord(player, name);
            case "reelplay" -> handlePlay(player, name);
            default -> false;
        };
    }

    private boolean handleRecord(Player player, String name) {
        MinecraftRecording active = recordings.remove(player.getUniqueId());
        if (active != null) {
            long frames = active.getCurrentFrame();
            active.stop();
            player.sendMessage(ChatColor.GREEN + "Stopped recording. Saved " + frames + " frames to " + name + ".reel.");
            return true;
        }

        File file = new File(getDataFolder(), name + ".reel");
        MinecraftRecording recording;
        try {
            recording = CraftReel.record()
                .setPlugin(this)
                .setReelWriter(w -> w.setFile(file).setLayerStrategy(new LayerStrategy(new long[]{1, 4, 16})))
                .build();
            recording.start();

            int centerChunkX = player.getLocation().getBlockX() >> 4;
            int centerChunkZ = player.getLocation().getBlockZ() >> 4;
            WorldRecorder worldRecorder = WorldRecorder.attach(recording, Identifier.random("world"), player.getWorld(),
                centerChunkX - 1, centerChunkZ - 1, centerChunkX + 2, centerChunkZ + 2,
                player.getLocation().toVector());

            SidebarRecorder sidebarRecorder = SidebarRecorder.attachDefault(recording);

            recording.addTickListener(() -> {
                if (!player.isOnline()) {
                    return;
                }
                int chunkX = player.getLocation().getBlockX() >> 4;
                int chunkZ = player.getLocation().getBlockZ() >> 4;
                worldRecorder.setBounds(chunkX - 1, chunkZ - 1, chunkX + 2, chunkZ + 2);

                long seconds = recording.getCurrentFrame() / 20L;
                String duration = String.format("%02d:%02d", seconds / 60, seconds % 60);
                sidebarRecorder.recordSidebar(List.of(
                    Component.text("REPLAYS", NamedTextColor.GOLD, TextDecoration.BOLD),
                    Component.text("Duration", NamedTextColor.GRAY)
                        .append(Component.text(": ", NamedTextColor.GRAY))
                        .append(Component.text(duration, NamedTextColor.WHITE)),
                    Component.text("Player", NamedTextColor.GRAY)
                        .append(Component.text(": ", NamedTextColor.GRAY))
                        .append(Component.text(player.getName(), NamedTextColor.WHITE))
                ));
            });

            TextRecorder.attachDefault(recording)
                .title(Component.text("Replay started", NamedTextColor.GREEN, TextDecoration.BOLD),
                    Component.empty(), 10, 40, 10);
        } catch (RuntimeException e) {
            player.sendMessage(ChatColor.RED + "Failed to start recording: " + e.getMessage());
            getLogger().warning("Failed to start recording " + file + ": " + e);
            return true;
        }

        recordings.put(player.getUniqueId(), recording);
        player.sendMessage(ChatColor.GREEN + "Recording to " + file.getName() + ". Run /reelrecord again to stop.");
        return true;
    }

    private boolean handlePlay(Player player, String name) {
        MinecraftReplay active = replays.remove(player.getUniqueId());
        if (active != null) {
            active.removeViewer(player);
            active.close();
            player.sendMessage(ChatColor.YELLOW + "Left the replay.");
            return true;
        }

        File file = new File(getDataFolder(), name + ".reel");
        if (!file.isFile()) {
            player.sendMessage(ChatColor.RED + "No recording named '" + name + "'. Record one with /reelrecord " + name + ".");
            return true;
        }

        MinecraftReplay replay;
        try {
            replay = CraftReel.replay()
                .setPlugin(this)
                .addDefaultHandlers()
                .setReelReader(r -> r.setFile(file))
                .setLooping(true)
                .build();
            replay.start();
            replay.addViewer(player);
        } catch (RuntimeException e) {
            player.sendMessage(ChatColor.RED + "Failed to start replay: " + e.getMessage());
            getLogger().warning("Failed to start replay " + file + ": " + e);
            return true;
        }

        replays.put(player.getUniqueId(), replay);
        player.sendMessage(ChatColor.GREEN + "Playing '" + name + "'. Run /reelplay again to leave.");
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MinecraftReplay replay = replays.remove(player.getUniqueId());
        if (replay != null) {
            replay.removeViewer(player);
            replay.close();
        }

        MinecraftRecording recording = recordings.remove(player.getUniqueId());
        if (recording != null) {
            recording.stop();
        }
    }

}
