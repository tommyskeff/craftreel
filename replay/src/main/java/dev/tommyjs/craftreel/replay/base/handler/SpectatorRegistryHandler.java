package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import org.jetbrains.annotations.NotNull;

public class SpectatorRegistryHandler extends ReplayHandler {

    @Override
    public void onPlayerReady(@NotNull ScenePlayer player) {
        getReplay().getScene().getResourceManager().publish(BaseResources.SPECTATORS, "spectators",
            getReplay().getSpectators());
    }

}
