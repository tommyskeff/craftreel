package dev.tommyjs.craftreel.replay;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;

public final class ReplayDiagnosticsImpl implements ReplayDiagnostics {

    private final long windowNanos;
    private final ArrayDeque<long[]> frames = new ArrayDeque<>();

    private volatile Seek lastSeek;

    public ReplayDiagnosticsImpl(Duration window) {
        this.windowNanos = window.toNanos();
    }

    public synchronized void recordFrame(long durationNanos) {
        long now = System.nanoTime();
        frames.addLast(new long[]{now, durationNanos});
        long cutoff = now - windowNanos;
        while (!frames.isEmpty() && frames.peekFirst()[0] < cutoff) {
            frames.pollFirst();
        }
    }

    public void recordSeek(long fromFrame, long toFrame, long durationNanos) {
        lastSeek = new SeekImpl(fromFrame, toFrame, durationNanos);
    }

    @Override
    public synchronized long frameTimePercentileNanos(double percentile) {
        if (frames.isEmpty()) {
            return 0L;
        }
        long[] durations = new long[frames.size()];
        int i = 0;
        for (long[] frame : frames) {
            durations[i++] = frame[1];
        }
        Arrays.sort(durations);
        int index = (int) Math.ceil(percentile / 100.0 * durations.length) - 1;
        return durations[Math.max(0, Math.min(durations.length - 1, index))];
    }

    @Override
    public synchronized int frameSampleCount() {
        return frames.size();
    }

    @Override
    public @Nullable Seek lastSeek() {
        return lastSeek;
    }

    private record SeekImpl(long fromFrame, long toFrame, long durationNanos) implements Seek {
    }

}
