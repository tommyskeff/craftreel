package dev.tommyjs.craftreel.replay;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.event.ReplayStartEvent;
import dev.tommyjs.craftreel.replay.event.ReplayStopEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.reel.replay.ReplayCursorBuilder;
import dev.tommyjs.reel.scene.ReplayScene;
import dev.tommyjs.reel.scene.ReplaySceneBuilder;
import dev.tommyjs.reel.scene.ReplaySceneImpl;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.scene.SceneResourceKey;
import dev.tommyjs.reel.storage.reader.ReelReaderBuilder;
import dev.tommyjs.reel.track.TrackRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractMinecraftReplay implements MinecraftReplay {

    protected static final Duration FRAME = Duration.ofMillis(50);

    private final Plugin plugin;
    private final ReplaySceneImpl scene;
    private final List<SceneHandler> handlers;
    private final Set<UUID> viewers;
    private final Map<UUID, ReplaySpectatorImpl> spectators;
    private final SpectatorRegistry spectatorRegistry;
    private final ReplayDiagnosticsImpl diagnostics;

    private BukkitTask task;

    protected AbstractMinecraftReplay(Plugin plugin,
                                      TrackRegistry trackRegistry,
                                      List<SceneHandler> handlers,
                                      Consumer<ReplaySceneBuilder> sceneConfig,
                                      Consumer<ReplayCursorBuilder> cursorConfig,
                                      Consumer<ReelReaderBuilder> readerConfig) {
        this.plugin = plugin;
        this.handlers = handlers;
        this.viewers = ConcurrentHashMap.newKeySet();
        this.spectators = new ConcurrentHashMap<>();
        this.spectatorRegistry = new SpectatorRegistryImpl();
        this.diagnostics = new ReplayDiagnosticsImpl(Duration.ofSeconds(10));

        ReplaySceneBuilder sb = ReplayScene.builder().setTrackRegistry(trackRegistry);
        if (readerConfig != null) {
            sb.setReader(readerConfig);
        }
        if (cursorConfig != null) {
            sb.setCursor(cursorConfig);
        }
        for (SceneHandler h : handlers) {
            if (h instanceof ReplayHandler) {
                ((ReplayHandler) h).bind(this);
            }
            sb.addHandler(h);
        }

        if (sceneConfig != null) {
            sceneConfig.accept(sb);
        }
        this.scene = sb.build();
    }

    protected final void initialise() {
        for (SceneHandler h : handlers) {
            if (h instanceof ReplayHandler) {
                ((ReplayHandler) h).onPlayerReady(getScenePlayer());
            }
        }
    }

    protected abstract void tick();

    protected void onStart() {
    }

    @Override
    public final void start() {
        if (task != null) {
            return;
        }
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                long start = System.nanoTime();
                tick();
                diagnostics.recordFrame(System.nanoTime() - start);
            }
        }.runTaskTimer(plugin, 1L, 1L);
        onStart();
        Bukkit.getPluginManager().callEvent(new ReplayStartEvent(this));
    }

    @Override
    public void stop() {
        getScenePlayer().pause();
        if (task != null) {
            task.cancel();
            task = null;
        }
        Bukkit.getPluginManager().callEvent(new ReplayStopEvent(this));
    }

    @Override
    public ReplaySceneImpl getScene() {
        return scene;
    }

    @Override
    public ReplayDiagnostics getDiagnostics() {
        return diagnostics;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void addViewer(Player player) {
        if (viewers.add(player.getUniqueId())) {
            ReplaySpectatorImpl spectator = new ReplaySpectatorImpl(player);
            spectators.put(player.getUniqueId(), spectator);
            Bukkit.getPluginManager().callEvent(new PlayerJoinReplayEvent(this, player, spectator));
        }
    }

    @Override
    public void removeViewer(Player player) {
        if (viewers.remove(player.getUniqueId())) {
            ReplaySpectatorImpl spectator = spectators.remove(player.getUniqueId());
            if (spectator != null) {
                spectator.detachAll();
            }
            Bukkit.getPluginManager().callEvent(new PlayerLeaveReplayEvent(this, player));
        }
    }

    @Override
    public void transferViewer(Player player, MinecraftReplay target, ViewerWorldChange worldChange) {
        if (target == this) {
            return;
        }
        if (!(target instanceof AbstractMinecraftReplay to) || !viewers.contains(player.getUniqueId())
            || to.isViewer(player)) {
            removeViewer(player);
            target.addViewer(player);
            return;
        }

        ViewerHandover handover = new ViewerHandover(player, worldChange);
        viewers.remove(player.getUniqueId());
        ReplaySpectatorImpl spectator = spectators.remove(player.getUniqueId());
        if (spectator != null) {
            spectator.handOffAll(handover);
        }
        Bukkit.getPluginManager().callEvent(new PlayerLeaveReplayEvent(this, player, handover));
        to.adopt(player, handover);
        handover.finish();
    }

    private void adopt(Player player, ViewerHandover handover) {
        viewers.add(player.getUniqueId());
        ReplaySpectatorImpl spectator = new ReplaySpectatorImpl(player);
        spectators.put(player.getUniqueId(), spectator);
        spectator.handover = handover;
        try {
            Bukkit.getPluginManager().callEvent(new PlayerJoinReplayEvent(this, player, spectator, handover));
        } finally {
            spectator.handover = null;
        }
    }

    @Override
    public Collection<Player> getOnlineViewers() {
        List<Player> online = new ArrayList<>();
        for (UUID id : viewers) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    @Override
    public SpectatorRegistry getSpectators() {
        return spectatorRegistry;
    }

    @Override
    public Set<UUID> getViewers() {
        return Set.copyOf(viewers);
    }

    @Override
    public boolean isViewer(Player player) {
        return viewers.contains(player.getUniqueId());
    }

    @Override
    public void close() {
        stop();
        scene.close();
        getScenePlayer().close();
    }

    private final class ReplaySpectatorImpl implements ReplaySpectator {

        private final Player player;
        private final Set<ViewerContext> attached = new LinkedHashSet<>();
        private ViewerHandover handover;

        private ReplaySpectatorImpl(Player player) {
            this.player = player;
        }

        @Override
        public @NotNull Player player() {
            return player;
        }

        @Override
        public void attach(@NotNull ViewerContext context) {
            if (!attached.add(context)) {
                return;
            }
            if (handover != null) {
                context.takeOver(player, handover);
            } else {
                context.addViewer(player);
            }
        }

        @Override
        public void detach(@NotNull ViewerContext context) {
            if (attached.remove(context)) {
                context.removeViewer(player);
            }
        }

        @Override
        public @NotNull Collection<ViewerContext> attached() {
            return new ArrayList<>(attached);
        }

        @Override
        public void attach(@NotNull SceneResourceKey<Identifier, ? extends ViewerContext> key, @NotNull Identifier id) {
            attach(scene.getResourceManager().require(key, id));
        }

        private void detachAll() {
            for (ViewerContext context : new ArrayList<>(attached)) {
                context.removeViewer(player);
            }
            attached.clear();
        }

        private void handOffAll(ViewerHandover handover) {
            for (ViewerContext context : new ArrayList<>(attached)) {
                context.handOff(player, handover);
            }
            attached.clear();
        }

        private void purge(ViewerContext context) {
            attached.remove(context);
        }

    }

    private final class SpectatorRegistryImpl implements SpectatorRegistry {

        @Override
        public @NotNull Collection<ReplaySpectator> spectators() {
            return new ArrayList<>(spectators.values());
        }

        @Override
        public void purge(@NotNull ViewerContext context) {
            for (ReplaySpectatorImpl spectator : spectators.values()) {
                spectator.purge(context);
            }
        }

    }

}
