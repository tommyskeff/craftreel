package dev.tommyjs.craftreel.replay.base.actor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class EntityPlaybackConfig {

    public static final EntityPlaybackConfig DEFAULT = builder().build();

    private final double renderDistance;
    private final @Nullable Duration tablistLinger;

    private EntityPlaybackConfig(Builder builder) {
        this.renderDistance = builder.renderDistance;
        this.tablistLinger = builder.tablistLinger;
    }

    public double renderDistance() {
        return renderDistance;
    }

    public @Nullable Duration tablistLinger() {
        return tablistLinger;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private double renderDistance = 48.0;
        private @Nullable Duration tablistLinger = Duration.ofSeconds(3);

        private Builder() {
        }

        public @NotNull Builder renderDistance(double renderDistance) {
            this.renderDistance = renderDistance;
            return this;
        }

        public @NotNull Builder tablistLinger(@Nullable Duration tablistLinger) {
            this.tablistLinger = tablistLinger;
            return this;
        }

        public @NotNull Builder keepTablistEntry() {
            this.tablistLinger = null;
            return this;
        }

        public @NotNull EntityPlaybackConfig build() {
            return new EntityPlaybackConfig(this);
        }

    }

}
