package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpectatorIsolationHandler extends ReplayHandler {

    @Override
    protected void onLoad(SceneHandlerContext ctx) {
        for (Player viewer : getReplay().getOnlineViewers()) {
            isolate(viewer);
        }
    }

    @Override
    protected void onStop() {
        for (Player viewer : getReplay().getOnlineViewers()) {
            reveal(viewer);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() == getReplay()) {
            isolate(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveReplayEvent event) {
        if (event.getReplay() == getReplay() && event.getPlayer().isOnline()) {
            reveal(event.getPlayer());
        }
    }

    @EventHandler
    public void onServerJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        if (isViewer(joined)) {
            return;
        }

        for (Player viewer : getReplay().getOnlineViewers()) {
            hide(viewer, joined);
        }
    }

    protected boolean shouldHide(Player viewer, Player other) {
        return !isViewer(other);
    }

    private void isolate(Player viewer) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(viewer) && shouldHide(viewer, other)) {
                hide(viewer, other);
            }
        }
    }

    private void reveal(Player viewer) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(viewer)) {
                viewer.showPlayer(other);
                other.showPlayer(viewer);
            }
        }
    }

    private void hide(Player viewer, Player other) {
        viewer.hidePlayer(other);
        other.hidePlayer(viewer);
    }

}
