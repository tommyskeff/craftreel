package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.MinecraftReplayBuilder;
import dev.tommyjs.reel.recorder.ReelRecorder;
import dev.tommyjs.reel.recorder.ReelRecorderBuilder;
import dev.tommyjs.reel.storage.writer.ReelWriterBuilder;
import dev.tommyjs.reel.track.TrackRegistry;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.function.Consumer;

public class MinecraftRecordingBuilder {

    private Plugin plugin;
    private TrackRegistry trackRegistry;
    private ReelRecorder recorder;
    private Consumer<ReelRecorderBuilder> recorderConfig;
    private Consumer<ReelWriterBuilder> writerConfig;

    public MinecraftRecordingBuilder setPlugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public MinecraftRecordingBuilder setTrackRegistry(TrackRegistry trackRegistry) {
        this.trackRegistry = trackRegistry;
        return this;
    }

    public MinecraftRecordingBuilder setRecorder(ReelRecorder recorder) {
        this.recorder = recorder;
        return this;
    }

    public MinecraftRecordingBuilder setReelRecorder(Consumer<ReelRecorderBuilder> recorderConfig) {
        this.recorderConfig = recorderConfig;
        return this;
    }

    public MinecraftRecordingBuilder setReelWriter(Consumer<ReelWriterBuilder> writerConfig) {
        this.writerConfig = writerConfig;
        return this;
    }

    public MinecraftRecording build() {
        Objects.requireNonNull(plugin, "plugin must be set");

        if (trackRegistry == null) {
            trackRegistry = CraftReelProtocol.defaultTrackRegistry().build();
        }

        if (recorder == null) {
            if (writerConfig == null) {
                throw new IllegalStateException("a reel writer or recorder must be provided");
            }

            ReelRecorderBuilder recorderBuilder = ReelRecorder.builder();
            if (recorderConfig != null) {
                recorderConfig.accept(recorderBuilder);
            }

            recorderBuilder.setWriter(writerConfig);
            recorderBuilder.setTrackRegistry(trackRegistry);
            recorder = recorderBuilder.build();
        }

        return new MinecraftRecordingImpl(plugin, recorder);
    }

}
