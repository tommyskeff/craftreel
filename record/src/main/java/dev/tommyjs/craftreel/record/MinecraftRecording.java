package dev.tommyjs.craftreel.record;

import dev.tommyjs.reel.recorder.ReelRecorder;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.util.function.Function;

public interface MinecraftRecording extends Closeable {

    static MinecraftRecordingBuilder builder() {
        return new MinecraftRecordingBuilder();
    }

    void start();

    void stop();

    boolean isRunning();

    ReelRecorder getRecorder();

    Plugin getPlugin();

    long getCurrentFrame();

    void addTickListener(Runnable listener);

    void addStopListener(Runnable listener);

    <T> T getDefault(Class<T> type, Function<? super MinecraftRecording, ? extends T> factory);

    @Override
    void close();

}
