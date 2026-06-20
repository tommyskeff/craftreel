package dev.tommyjs.craftreel.replay.handler;

import dev.tommyjs.craftreel.replay.MinecraftReplay;
import dev.tommyjs.reel.replay.update.ReplayUpdate;
import dev.tommyjs.reel.scene.ReplayScene;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import dev.tommyjs.reel.scene.SceneTransition;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class ReplayHandler implements SceneHandler, Listener {

    protected MinecraftReplay replay;

    public void bind(MinecraftReplay replay) {
        this.replay = replay;
    }

    @Override
    public final void load(@NotNull SceneHandlerContext ctx) {
        Bukkit.getPluginManager().registerEvents(this, replay.getPlugin());
        onLoad(ctx);
    }

    @Override
    public void run(@NotNull ReplayUpdate update, @NotNull SceneTransition transition, @NotNull ReplayScene scene) {
    }

    @Override
    public final void stop(@NotNull ReplayScene scene) {
        HandlerList.unregisterAll(this);
        onStop();
    }

    protected void onLoad(SceneHandlerContext ctx) {
    }

    protected void onStop() {
    }

    public void onPlayerReady(@NotNull ScenePlayer player) {
    }

    protected MinecraftReplay getReplay() {
        return replay;
    }

    protected boolean isViewer(Player p) {
        return replay != null && replay.isViewer(p);
    }

}
