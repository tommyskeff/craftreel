package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.record.event.RecordingStartEvent;
import dev.tommyjs.craftreel.record.event.RecordingStopEvent;
import dev.tommyjs.reel.recorder.ReelRecorder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MinecraftRecordingImpl implements MinecraftRecording {

    private final Plugin plugin;
    private final ReelRecorder recorder;
    private final List<Runnable> stopListeners = new ArrayList<>();
    private final List<Runnable> tickListeners = new ArrayList<>();

    private BukkitTask task;
    private boolean running;

    public MinecraftRecordingImpl(Plugin plugin, ReelRecorder recorder) {
        this.plugin = plugin;
        this.recorder = recorder;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Runnable listener : new ArrayList<>(tickListeners)) {
                    try {
                        listener.run();
                    } catch (Throwable t) {
                        plugin.getLogger().log(Level.WARNING, "craftreel tick listener failed", t);
                    }
                }
                recorder.advanceFrame();
            }
        }.runTaskTimer(plugin, 1L, 1L);

        Bukkit.getPluginManager().callEvent(new RecordingStartEvent(this));
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        if (task != null) {
            task.cancel();
            task = null;
        }

        for (Runnable listener : new ArrayList<>(stopListeners)) {
            try {
                listener.run();
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "craftreel stop listener failed", t);
            }
        }

        try {
            recorder.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "failed to close reel recorder", e);
        }

        Bukkit.getPluginManager().callEvent(new RecordingStopEvent(this));
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public ReelRecorder getRecorder() {
        return recorder;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public long getCurrentFrame() {
        return recorder.currentFrame();
    }

    @Override
    public void addTickListener(Runnable listener) {
        tickListeners.add(listener);
    }

    @Override
    public void addStopListener(Runnable listener) {
        stopListeners.add(listener);
    }

    @Override
    public void close() {
        stop();
    }

}
