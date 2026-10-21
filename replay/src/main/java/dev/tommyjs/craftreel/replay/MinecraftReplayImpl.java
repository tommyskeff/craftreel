package dev.tommyjs.craftreel.replay;

import dev.tommyjs.reel.replay.ReplayCursorBuilder;
import dev.tommyjs.reel.scene.ReplaySceneBuilder;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import dev.tommyjs.reel.storage.reader.ReelReaderBuilder;
import dev.tommyjs.reel.track.TrackRegistry;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Consumer;

public class MinecraftReplayImpl extends AbstractMinecraftReplay {

    private final ScenePlayer player;

    public MinecraftReplayImpl(Plugin plugin,
                               TrackRegistry trackRegistry,
                               List<SceneHandler> handlers,
                               Consumer<ReplaySceneBuilder> sceneConfig,
                               Consumer<ReplayCursorBuilder> cursorConfig,
                               Consumer<ReelReaderBuilder> readerConfig,
                               boolean looping,
                               double speed) {
        super(plugin, trackRegistry, handlers, sceneConfig, cursorConfig, readerConfig);
        this.player = ScenePlayer.builder()
            .setScene(getScene())
            .setBaseFps(20)
            .setSpeed(speed)
            .setLooping(looping)
            .build();
        initialise();
    }

    @Override
    public ScenePlayer getScenePlayer() {
        return player;
    }

    @Override
    protected void tick() {
        player.tick(FRAME);
    }

}
