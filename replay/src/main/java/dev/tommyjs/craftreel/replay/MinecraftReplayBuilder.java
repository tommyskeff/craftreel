package dev.tommyjs.craftreel.replay;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.base.DefaultHandlers;
import dev.tommyjs.reel.replay.ReplayCursorBuilder;
import dev.tommyjs.reel.scene.ReplaySceneBuilder;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.storage.reader.ReelReaderBuilder;
import dev.tommyjs.reel.track.TrackRegistry;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MinecraftReplayBuilder {

    private Plugin plugin;
    private TrackRegistry trackRegistry;
    private final List<SceneHandler> handlers = new ArrayList<>();
    private Consumer<ReplaySceneBuilder> sceneConfig;
    private Consumer<ReplayCursorBuilder> cursorConfig;
    private Consumer<ReelReaderBuilder> readerConfig;
    private boolean looping = false;
    private double speed = 1.0;

    public MinecraftReplayBuilder setPlugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public MinecraftReplayBuilder setTrackRegistry(TrackRegistry trackRegistry) {
        this.trackRegistry = trackRegistry;
        return this;
    }

    public MinecraftReplayBuilder setReplayScene(Consumer<ReplaySceneBuilder> sceneConfig) {
        this.sceneConfig = sceneConfig;
        return this;
    }

    public MinecraftReplayBuilder setReplayCursor(Consumer<ReplayCursorBuilder> cursorConfig) {
        this.cursorConfig = cursorConfig;
        return this;
    }

    public MinecraftReplayBuilder setReelReader(Consumer<ReelReaderBuilder> readerConfig) {
        this.readerConfig = readerConfig;
        return this;
    }

    public MinecraftReplayBuilder addHandler(SceneHandler handler) {
        this.handlers.add(handler);
        return this;
    }

    public MinecraftReplayBuilder removeHandlers(Class<? extends SceneHandler> type) {
        this.handlers.removeIf(type::isInstance);
        return this;
    }

    public MinecraftReplayBuilder replaceHandlers(Class<? extends SceneHandler> type, SceneHandler handler) {
        removeHandlers(type);
        this.handlers.add(handler);
        return this;
    }

    public MinecraftReplayBuilder addDefaultHandlers() {
        this.handlers.addAll(DefaultHandlers.DEFAULT_HANDLERS);
        return this;
    }

    public MinecraftReplayBuilder setLooping(boolean looping) {
        this.looping = looping;
        return this;
    }

    public MinecraftReplayBuilder setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public MinecraftReplay build() {
        Objects.requireNonNull(plugin, "plugin must be set");

        if (trackRegistry == null) {
            trackRegistry = CraftReelProtocol.defaultTrackRegistry().build();
        }

        if (sceneConfig == null && cursorConfig == null && readerConfig == null) {
            throw new IllegalStateException("a reel reader, cursor, or scene config must be provided");
        }

        return new MinecraftReplayImpl(plugin, trackRegistry, handlers, sceneConfig, cursorConfig, readerConfig, looping, speed);
    }

}
