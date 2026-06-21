package dev.tommyjs.craftreelexample;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.craftreel.record.ObjectiveHandle;
import dev.tommyjs.craftreel.record.ScoreboardRecorder;
import dev.tommyjs.craftreel.record.SidebarRecorder;
import dev.tommyjs.craftreel.record.TabHeaderRecorder;
import dev.tommyjs.craftreel.record.TabListRecorder;
import dev.tommyjs.craftreel.record.TeamHandle;
import dev.tommyjs.craftreel.record.TeamRecorder;
import dev.tommyjs.craftreel.record.TextRecorder;
import dev.tommyjs.craftreel.record.WorldRecorder;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardRenderType;
import dev.tommyjs.craftreel.protocol.team.TeamInfo;
import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.util.Identifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import dev.tommyjs.reel.storage.writer.LayerStrategy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            recording = MinecraftRecording.builder()
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

            TeamRecorder teamRecorder = TeamRecorder.attachDefault(recording);
            TeamHandle team = teamRecorder.createTeam("recorder");
            team.setInfo(TeamInfo.builder("recorder")
                .setPrefix(Component.text("[TARGET] ", NamedTextColor.AQUA))
                .setColor(NamedTextColor.RED)
                .build());
            team.addMembers(Set.of(player.getName()));

            TabHeaderRecorder tabHeaderRecorder = TabHeaderRecorder.attachDefault(recording);
            tabHeaderRecorder.recordHeader(
                Component.text("CraftReel Replay", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("Recorded by " + player.getName(), NamedTextColor.GRAY));

            TabListRecorder tabListRecorder = TabListRecorder.attachDefault(recording);
            String[] recorderSkin = extractSkin(player);
            Component recorderDisplay = Component.text("[TARGET] ", NamedTextColor.YELLOW)
                .append(Component.text(player.getName(), NamedTextColor.RED));
            tabListRecorder.recordTabEntry(extractProfileId(player), player.getName(),
                recorderSkin == null ? null : recorderSkin[0], recorderSkin == null ? null : recorderSkin[1],
                recorderDisplay, 10, 0);

            // Standalone entries not tied to any recorded player entity.
            tabListRecorder.recordTabEntry(UUID.randomUUID(), "Notch", null, null,
                Component.text("[ADMIN] ", NamedTextColor.RED).append(Component.text("Notch", NamedTextColor.WHITE)), 25, 1);
            tabListRecorder.recordTabEntry(UUID.randomUUID(), "Herobrine", null, null,
                Component.text("[SPEC] ", NamedTextColor.GRAY).append(Component.text("Herobrine", NamedTextColor.GRAY)), 80, 3);

            ScoreboardRecorder scoreboardRecorder = ScoreboardRecorder.attachDefault(recording);
            ObjectiveHandle healthObjective = scoreboardRecorder.createObjective("health");
            healthObjective.setInfo(Component.text("❤", NamedTextColor.RED), ScoreboardRenderType.INTEGER, 2);

            // Create the default text context at frame 0 so chat/title contexts exist
            // when a spectator joins (matches the eager attach of the other contexts).
            TextRecorder.attachDefault(recording);

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

                healthObjective.setScore(player.getName(), (int) Math.round(player.getHealth()));

                if (recording.getCurrentFrame() == 20L) {
                    TextRecorder.attachDefault(recording)
                        .title(Component.text("Replay started", NamedTextColor.GREEN, TextDecoration.BOLD),
                            Component.empty(), 10, 40, 10);
                }
            });
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
            replay = MinecraftReplay.builder()
                .setPlugin(this)
                .addDefaultHandlers()
                .setReelReader(r -> r.setFile(file))
                .setLooping(false)
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

    private static UUID extractProfileId(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object profile = handle.getClass().getMethod("getProfile").invoke(handle);
            Object id = profile.getClass().getMethod("getId").invoke(profile);
            if (id instanceof UUID uuid) {
                return uuid;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        return player.getUniqueId();
    }

    private static String[] extractSkin(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object profile = handle.getClass().getMethod("getProfile").invoke(handle);
            Object properties = profile.getClass().getMethod("getProperties").invoke(profile);
            Object textures = properties.getClass().getMethod("get", Object.class).invoke(properties, "textures");
            for (Object property : (Iterable<?>) textures) {
                String value = (String) property.getClass().getMethod("getValue").invoke(property);
                String signature = (String) property.getClass().getMethod("getSignature").invoke(property);
                return new String[]{value, signature};
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        return null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (recordings.isEmpty()) {
            return;
        }
        String line = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        Bukkit.getScheduler().runTask(this, () -> {
            for (MinecraftRecording recording : recordings.values()) {
                TextRecorder.attachDefault(recording).recordChat(Component.text(line));
            }
        });
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
