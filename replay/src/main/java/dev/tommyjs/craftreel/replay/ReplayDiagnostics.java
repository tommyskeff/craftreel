package dev.tommyjs.craftreel.replay;

import org.jetbrains.annotations.Nullable;

public interface ReplayDiagnostics {

    long frameTimePercentileNanos(double percentile);

    int frameSampleCount();

    @Nullable Seek lastSeek();

    interface Seek {

        long fromFrame();

        long toFrame();

        long durationNanos();

    }

}
