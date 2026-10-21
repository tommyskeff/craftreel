package dev.tommyjs.craftreel.replay;

import dev.tommyjs.reel.replay.ReplayCursorBuilder;
import dev.tommyjs.reel.scene.ReplaySceneBuilder;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.scene.player.LiveScenePlayer;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import dev.tommyjs.reel.storage.reader.ReelReaderBuilder;
import dev.tommyjs.reel.track.TrackRegistry;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public class LiveMinecraftReplayImpl extends AbstractMinecraftReplay implements LiveMinecraftReplay {

    private final LiveScenePlayer player;
    private final long catchUpFrames;

    private boolean followingLive = true;

    public LiveMinecraftReplayImpl(Plugin plugin,
                                   TrackRegistry trackRegistry,
                                   List<SceneHandler> handlers,
                                   Consumer<ReplaySceneBuilder> sceneConfig,
                                   Consumer<ReplayCursorBuilder> cursorConfig,
                                   Consumer<ReelReaderBuilder> readerConfig,
                                   double speed,
                                   Duration liveDelay,
                                   Duration catchUpThreshold) {
        super(plugin, trackRegistry, handlers, sceneConfig, cursorConfig, readerConfig);
        this.player = ScenePlayer.builder()
            .setScene(getScene())
            .setBaseFps(20)
            .setSpeed(speed)
            .setLiveDelay(liveDelay)
            .buildLive();
        this.catchUpFrames = Math.max(1L, catchUpThreshold.toMillis() / FRAME.toMillis());
        initialise();
    }

    @Override
    public LiveScenePlayer getScenePlayer() {
        return player;
    }

    @Override
    public @NotNull Duration getLiveDelay() {
        return player.getLiveDelay();
    }

    @Override
    public void setLiveDelay(@NotNull Duration delay) {
        player.setLiveDelay(delay);
    }

    @Override
    public boolean isCaughtUp() {
        return player.getLiveTargetFrame() - player.getCurrentFrame() <= catchUpFrames;
    }

    @Override
    public boolean isFollowingLive() {
        return followingLive;
    }

    @Override
    public void setFollowingLive(boolean followingLive) {
        this.followingLive = followingLive;
        if (followingLive) {
            player.seek(player.getLiveTargetFrame());
            player.play();
        }
    }

    @Override
    protected void onStart() {
        player.play();
    }

    @Override
    protected void tick() {
        player.tick(FRAME);
        if (!followingLive) {
            return;
        }

        long target = player.getLiveTargetFrame();
        if (target - player.getCurrentFrame() > catchUpFrames) {
            player.seek(target);
        }
    }

}
