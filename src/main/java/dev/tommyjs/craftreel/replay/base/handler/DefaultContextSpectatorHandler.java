package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.base.SpectatorHelper;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
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

        if (spectator.get(BaseResources.Slots.SIDEBAR) == null) {
            ViewerContext sidebar = resources.find(BaseResources.SIDEBAR, CraftReelProtocol.Defaults.SIDEBAR);
            if (sidebar != null) {
                SpectatorHelper.setSidebar(spectator, sidebar);
            }
        }

        if (spectator.get(BaseResources.Slots.TAB_HEADER) == null) {
            ViewerContext tabHeader = resources.find(BaseResources.TAB_HEADER, CraftReelProtocol.Defaults.TAB_HEADER);
            if (tabHeader != null) {
                SpectatorHelper.setTabHeader(spectator, tabHeader);
            }
        }

        ViewerContext chat = resources.find(BaseResources.CHAT, CraftReelProtocol.Defaults.TEXT);
        if (chat != null) {
            SpectatorHelper.joinChat(spectator, chat);
        }

        ViewerContext title = resources.find(BaseResources.TITLE, CraftReelProtocol.Defaults.TEXT);
        if (title != null) {
            spectator.attach(title);
        }
    }

}
