package dev.tommyjs.craftreelexample;

import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.craftreel.record.MinecraftRecordingBuilder;
import dev.tommyjs.reel.recorder.ReelRecorderBuilder;
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
import dev.tommyjs.craftreel.replay.LiveMinecraftReplay;
import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.reel.scene.SceneResourceManager;
import dev.tommyjs.reel.storage.live.ReelHeadState;
import dev.tommyjs.craftreel.util.Identifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import dev.tommyjs.reel.storage.live.LocalLiveReel;
import dev.tommyjs.reel.storage.writer.LayerStrategy;
import dev.tommyjs.reel.storage.writer.ReelWriterBuilder;
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
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class CraftReelExamplePlugin extends JavaPlugin implements Listener {

    private static final LayerStrategy LAYERS = new LayerStrategy(new long[]{1, 4, 16});
    private static final Duration DEFAULT_LIVE_DELAY = Duration.ofSeconds(10);

    private final Map<UUID, MinecraftRecording> recordings = new HashMap<>();
    private final Map<UUID, MinecraftReplay> replays = new HashMap<>();
    private final Map<UUID, LiveSession> liveSessions = new HashMap<>();
    private final Map<UUID, UUID> watching = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getDataFolder().mkdirs();
    }

    @Override
    public void onDisable() {
        for (LiveSession session : List.copyOf(liveSessions.values())) {
            session.close();
        }
        liveSessions.clear();
        watching.clear();

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

        return switch (command.getName().toLowerCase()) {
            case "reelrecord" -> handleRecord(player, args.length > 0 ? args[0] : "demo");
            case "reelplay" -> handlePlay(player, args.length > 0 ? args[0] : "demo");
            case "reellive" -> handleLive(player, args);
            case "reelwatch" -> handleWatch(player, args);
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
            recording = startRecording(player, null, w -> w.setFile(file).setLayerStrategy(LAYERS));
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
                .addNavigation()
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

    private boolean handleLive(Player player, String[] args) {
        LiveSession active = liveSessions.remove(player.getUniqueId());
        if (active != null) {
            active.close();
            player.sendMessage(ChatColor.YELLOW + "Stopped the live replay.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            reportStatus(player);
            return true;
        }

        Duration delay = DEFAULT_LIVE_DELAY;
        if (args.length > 0) {
            try {
                delay = Duration.ofSeconds(Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a number of seconds.");
                return true;
            }
        }

        if (delay.isNegative()) {
            player.sendMessage(ChatColor.RED + "The delay must not be negative.");
            return true;
        }

        LiveSession session;
        try {
            session = startLive(player, delay);
        } catch (RuntimeException e) {
            player.sendMessage(ChatColor.RED + "Failed to start the live replay: " + e.getMessage());
            getLogger().warning("Failed to start live replay for " + player.getName() + ": " + e);
            return true;
        }

        liveSessions.put(player.getUniqueId(), session);
        player.sendMessage(ChatColor.GREEN + "Live replay of " + player.getName() + " started with a "
            + delay.getSeconds() + "s delay.");
        player.sendMessage(ChatColor.GRAY + "Watch it with /reelwatch " + player.getName()
            + ". Run /reellive again to stop.");
        return true;
    }

    private void reportStatus(Player player) {
        if (liveSessions.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No live replay is running.");
            return;
        }

        for (LiveSession session : liveSessions.values()) {
            String owner = Bukkit.getOfflinePlayer(session.owner()).getName();
            long recorded = session.recording().getCurrentFrame();
            long head = session.live().reader().getHeadFrame();
            long current = session.replay().getScenePlayer().getCurrentFrame();
            long target = session.replay().getScenePlayer().getLiveTargetFrame();

            player.sendMessage(ChatColor.AQUA + "Live replay of " + owner);
            player.sendMessage(ChatColor.GRAY + "  recorded frame: " + ChatColor.WHITE + recorded
                + ChatColor.GRAY + "  reader head: " + ChatColor.WHITE + head);
            player.sendMessage(ChatColor.GRAY + "  playhead: " + ChatColor.WHITE + current
                + ChatColor.GRAY + "  live target: " + ChatColor.WHITE + target);
            player.sendMessage(ChatColor.GRAY + "  behind head by " + ChatColor.WHITE
                + String.format("%.1fs", (head - current) / 20.0)
                + ChatColor.GRAY + " (delay " + session.replay().getLiveDelay().getSeconds() + "s, viewers "
                + session.viewers().size() + ")");

            SceneResourceManager resources = session.replay().getScene().getResourceManager();
            player.sendMessage(ChatColor.GRAY + "  worlds: " + ChatColor.WHITE
                + resources.getResources(BaseResources.WORLD).size()
                + ChatColor.GRAY + "  replay players: " + ChatColor.WHITE
                + resources.getResources(BaseResources.PLAYER).size());

            ReelHeadState state = session.live().reader().headState();
            player.sendMessage(ChatColor.GRAY + "  reader types: " + ChatColor.WHITE
                + state.types().types().size() + " entity / " + state.tracks().trackTypes().size() + " track"
                + ChatColor.GRAY + "  segments: " + ChatColor.WHITE + state.segments().segments().size());
            player.sendMessage(ChatColor.GRAY + "  scene active entities: " + ChatColor.WHITE
                + session.replay().getScene().getActiveEntities().size()
                + ChatColor.GRAY + "  scene frames: " + ChatColor.WHITE
                + session.replay().getScene().getTotalFrames());
        }
    }

    private boolean handleWatch(Player player, String[] args) {
        UUID active = watching.remove(player.getUniqueId());
        if (active != null) {
            LiveSession session = liveSessions.get(active);
            if (session != null) {
                session.removeViewer(player);
            }
            player.sendMessage(ChatColor.YELLOW + "Left the live replay.");
            return true;
        }

        LiveSession session;
        if (args.length > 0) {
            Player owner = Bukkit.getPlayerExact(args[0]);
            session = owner == null ? null : liveSessions.get(owner.getUniqueId());
            if (session == null) {
                player.sendMessage(ChatColor.RED + "'" + args[0] + "' has no live replay running.");
                return true;
            }
        } else if (liveSessions.size() == 1) {
            session = liveSessions.values().iterator().next();
        } else if (liveSessions.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No live replay is running. Start one with /reellive.");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Several live replays are running; name one: /reelwatch <player>.");
            return true;
        }

        if (session.owner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Heads up: you are the recorded player, so following your own "
                + "replay feeds your movement back into the recording.");
        }

        session.addViewer(player);
        watching.put(player.getUniqueId(), session.owner());
        player.sendMessage(ChatColor.GREEN + "Watching live with a " + session.replay().getLiveDelay().getSeconds()
            + "s delay. Use the compass in slot 1 to follow a player. Run /reelwatch again to leave.");
        return true;
    }

    private LiveSession startLive(Player player, Duration delay) {
        File file = new File(getDataFolder(), "live-" + player.getName() + ".reel");
        LocalLiveReel live = new LocalLiveReel(LAYERS);

        MinecraftRecording recording;
        LiveMinecraftReplay replay;
        try {
            recording = startRecording(player, null,
                w -> w.setFile(file).setLayerStrategy(LAYERS).addListener(live.publisher()));
            recording.addPostTickListener(live::flush);

            replay = MinecraftReplay.builder()
                .setPlugin(this)
                .addSceneHandlers()
                .addControls()
                .addNavigation()
                .setReplayCursor(c -> c.setReader(live.reader()))
                .setLiveDelay(delay)
                .buildLive();
        } catch (RuntimeException e) {
            live.close();
            throw e;
        }

        replay.start();
        return new LiveSession(recording, replay, live, player.getUniqueId(), new HashSet<>());
    }

    private MinecraftRecording startRecording(Player player, Consumer<ReelRecorderBuilder> recorderConfig,
                                              Consumer<ReelWriterBuilder> writerConfig) {
        MinecraftRecordingBuilder builder = MinecraftRecording.builder()
            .setPlugin(this)
            .setReelWriter(writerConfig);
        if (recorderConfig != null) {
            builder.setReelRecorder(recorderConfig);
        }

        MinecraftRecording recording = builder.build();
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
        tabListRecorder.recordTabEntry(UUID.randomUUID(), player.getName(),
            recorderSkin == null ? null : recorderSkin[0], recorderSkin == null ? null : recorderSkin[1],
            recorderDisplay, 10, 0);

        tabListRecorder.recordTabEntry(UUID.randomUUID(), "Notch", null, null,
            Component.text("[ADMIN] ", NamedTextColor.RED).append(Component.text("Notch", NamedTextColor.WHITE)), 25, 1);
        tabListRecorder.recordTabEntry(UUID.randomUUID(), "Herobrine", null, null,
            Component.text("[SPEC] ", NamedTextColor.GRAY).append(Component.text("Herobrine", NamedTextColor.GRAY)), 80, 3);

        ScoreboardRecorder scoreboardRecorder = ScoreboardRecorder.attachDefault(recording);
        ObjectiveHandle healthObjective = scoreboardRecorder.createObjective("health");
        healthObjective.setInfo(Component.text("❤", NamedTextColor.RED), ScoreboardRenderType.INTEGER, 2);

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

        return recording;
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
        if (recordings.isEmpty() && liveSessions.isEmpty()) {
            return;
        }
        String line = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        Bukkit.getScheduler().runTask(this, () -> {
            for (MinecraftRecording recording : recordings.values()) {
                TextRecorder.attachDefault(recording).recordChat(Component.text(line));
            }
            for (LiveSession session : liveSessions.values()) {
                TextRecorder.attachDefault(session.recording()).recordChat(Component.text(line));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID watched = watching.remove(player.getUniqueId());
        if (watched != null) {
            LiveSession watchedSession = liveSessions.get(watched);
            if (watchedSession != null) {
                watchedSession.removeViewer(player);
            }
        }

        LiveSession session = liveSessions.remove(player.getUniqueId());
        if (session != null) {
            session.close();
        }

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

    private record LiveSession(MinecraftRecording recording, LiveMinecraftReplay replay, LocalLiveReel live,
                               UUID owner, Set<UUID> viewers) {

        private void addViewer(Player player) {
            viewers.add(player.getUniqueId());
            replay.addViewer(player);
        }

        private void removeViewer(Player player) {
            viewers.remove(player.getUniqueId());
            replay.removeViewer(player);
        }

        private void close() {
            for (UUID id : Set.copyOf(viewers)) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) {
                    replay.removeViewer(player);
                }
            }
            viewers.clear();

            recording.stop();
            replay.close();
            live.close();
        }

    }

}
