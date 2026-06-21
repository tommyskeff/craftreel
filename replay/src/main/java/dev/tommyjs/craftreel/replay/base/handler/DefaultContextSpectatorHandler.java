package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.scene.SceneResourceKey;
import dev.tommyjs.reel.scene.SceneResourceManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class DefaultContextSpectatorHandler extends ReplayHandler {

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() != getReplay()) {
            return;
        }

        SceneResourceManager resources = getReplay().getScene().getResourceManager();
        ReplaySpectator spectator = event.getSpectator();

        attachDefault(resources, spectator, BaseResources.SIDEBAR, CraftReelProtocol.Defaults.SIDEBAR);
        attachDefault(resources, spectator, BaseResources.TAB_HEADER, CraftReelProtocol.Defaults.TAB_HEADER);
        attachDefault(resources, spectator, BaseResources.TAB_LIST, CraftReelProtocol.Defaults.TAB_LIST);
        attachDefault(resources, spectator, BaseResources.SCOREBOARD, CraftReelProtocol.Defaults.SCOREBOARD);
        attachDefault(resources, spectator, BaseResources.TEAM, CraftReelProtocol.Defaults.TEAM);
        attachDefault(resources, spectator, BaseResources.CHAT, CraftReelProtocol.Defaults.TEXT);
        attachDefault(resources, spectator, BaseResources.TITLE, CraftReelProtocol.Defaults.TEXT);
    }

    private void attachDefault(SceneResourceManager resources, ReplaySpectator spectator,
                               SceneResourceKey<Identifier, ? extends ViewerContext> key, Identifier id) {
        ViewerContext context = resources.find(key, id);
        if (context != null) {
            spectator.attach(context);
        }
    }

}
