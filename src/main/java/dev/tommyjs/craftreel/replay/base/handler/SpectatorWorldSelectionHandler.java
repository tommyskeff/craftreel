package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.base.SpectatorHelper;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Collection;

public class SpectatorWorldSelectionHandler extends ReplayHandler {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() != getReplay()) {
            return;
        }

        Collection<WorldContext> worlds = getReplay().getScene().getResourceManager().getResources(BaseResources.WORLD);
        if (worlds.isEmpty()) {
            return;
        }

        SpectatorHelper.setWorld(event.getSpectator(), selectWorld(worlds));
    }

    protected WorldContext selectWorld(Collection<WorldContext> worlds) {
        return worlds.iterator().next();
    }

}
