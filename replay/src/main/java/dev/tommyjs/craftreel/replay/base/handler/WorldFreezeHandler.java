package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.dynworld.entity.EntityTracker;
import dev.tommyjs.reel.scene.player.PlaybackListener;
import dev.tommyjs.reel.scene.player.PlaybackState;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import org.jetbrains.annotations.NotNull;

public class WorldFreezeHandler extends ReplayHandler {

    @Override
    public void onPlayerReady(@NotNull ScenePlayer player) {
        player.subscribe(new PlaybackListener() {
            @Override
            public void onState(@NotNull PlaybackState state) {
                boolean frozen = state != PlaybackState.PLAYING;
                for (WorldContext world : getReplay().getScene().getResourceManager().getResources(BaseResources.WORLD)) {
                    EntityTracker tracker = world.tracker();
                    if (frozen) {
                        tracker.freeze();
                        tracker.tick();
                    } else {
                        tracker.unfreeze();
                    }
                }
            }
        });
    }

}
