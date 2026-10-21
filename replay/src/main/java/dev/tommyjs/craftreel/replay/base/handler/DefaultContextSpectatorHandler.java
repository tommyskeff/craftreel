package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.replay.update.ReplayUpdate;
import dev.tommyjs.reel.scene.ReplayScene;
import dev.tommyjs.reel.scene.SceneResourceKey;
import dev.tommyjs.reel.scene.SceneResourceManager;
import dev.tommyjs.reel.scene.SceneTransition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultContextSpectatorHandler extends ReplayHandler {

    private static final List<Default> DEFAULTS = List.of(
        new Default(BaseResources.SIDEBAR, CraftReelProtocol.Defaults.SIDEBAR),
        new Default(BaseResources.TAB_HEADER, CraftReelProtocol.Defaults.TAB_HEADER),
        new Default(BaseResources.TAB_LIST, CraftReelProtocol.Defaults.TAB_LIST),
        new Default(BaseResources.SCOREBOARD, CraftReelProtocol.Defaults.SCOREBOARD),
        new Default(BaseResources.TEAM, CraftReelProtocol.Defaults.TEAM),
        new Default(BaseResources.CHAT, CraftReelProtocol.Defaults.TEXT),
        new Default(BaseResources.TITLE, CraftReelProtocol.Defaults.TEXT));

    private final Map<UUID, Set<Default>> pending = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() != getReplay()) {
            return;
        }

        Set<Default> missing = ConcurrentHashMap.newKeySet();
        missing.addAll(DEFAULTS);
        attach(event.getSpectator(), missing);
        if (!missing.isEmpty()) {
            pending.put(event.getPlayer().getUniqueId(), missing);
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveReplayEvent event) {
        if (event.getReplay() == getReplay()) {
            pending.remove(event.getPlayer().getUniqueId());
        }
    }

    @Override
    public void run(@NotNull ReplayUpdate update, @NotNull SceneTransition transition, @NotNull ReplayScene scene) {
        if (pending.isEmpty()) {
            return;
        }

        for (ReplaySpectator spectator : getReplay().getSpectators().spectators()) {
            UUID id = spectator.player().getUniqueId();
            Set<Default> missing = pending.get(id);
            if (missing == null) {
                continue;
            }

            attach(spectator, missing);
            if (missing.isEmpty()) {
                pending.remove(id);
            }
        }
    }

    private void attach(ReplaySpectator spectator, Set<Default> missing) {
        SceneResourceManager resources = getReplay().getScene().getResourceManager();
        for (Default entry : new ArrayList<>(missing)) {
            ViewerContext context = resources.find(entry.key(), entry.id());
            if (context != null) {
                spectator.attach(context);
                missing.remove(entry);
            }
        }
    }

    private record Default(SceneResourceKey<Identifier, ? extends ViewerContext> key, Identifier id) {
    }

}
