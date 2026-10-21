package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.reel.replay.update.ReplayUpdate;
import dev.tommyjs.reel.scene.ReplayScene;
import dev.tommyjs.reel.scene.SceneTransition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpectatorWorldSelectionHandler extends ReplayHandler {

    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() != getReplay()) {
            return;
        }

        if (!attach(event.getSpectator())) {
            pending.add(event.getPlayer().getUniqueId());
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
            if (pending.contains(id) && attach(spectator)) {
                pending.remove(id);
            }
        }
    }

    protected WorldContext selectWorld(Collection<WorldContext> worlds) {
        return worlds.iterator().next();
    }

    private boolean attach(ReplaySpectator spectator) {
        for (ViewerContext context : spectator.attached()) {
            if (context instanceof WorldContext) {
                return true;
            }
        }

        Collection<WorldContext> worlds = getReplay().getScene().getResourceManager().getResources(BaseResources.WORLD);
        if (worlds.isEmpty()) {
            return false;
        }

        spectator.attach(selectWorld(worlds));
        return true;
    }

}
