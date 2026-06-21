package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class SpectatorTrackerFlushHandler extends ReplayHandler {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() != getReplay() || getReplay().getScenePlayer().isPlaying()) {
            return;
        }

        for (WorldContext world : getReplay().getScene().getResourceManager().getResources(BaseResources.WORLD)) {
            world.tracker().tick();
        }
    }

}
