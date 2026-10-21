package dev.tommyjs.craftreel.replay;

import dev.tommyjs.reel.scene.player.LiveScenePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface LiveMinecraftReplay extends MinecraftReplay {

    @Override
    LiveScenePlayer getScenePlayer();

    @NotNull Duration getLiveDelay();

    void setLiveDelay(@NotNull Duration delay);

    boolean isCaughtUp();

    boolean isFollowingLive();

    void setFollowingLive(boolean followingLive);

}
