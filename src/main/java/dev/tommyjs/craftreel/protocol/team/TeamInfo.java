package dev.tommyjs.craftreel.protocol.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TeamInfo(@NotNull String name, @NotNull Component displayName, @NotNull Component prefix,
                       @NotNull Component suffix, @Nullable NamedTextColor color, boolean friendlyFire,
                       boolean seeFriendlyInvisibles, @NotNull NameTagVisibility nameTagVisibility,
                       @NotNull CollisionRule collisionRule) {

    public static @NotNull Builder builder(@NotNull String name) {
        return new Builder(name);
    }

    public static final class Builder {

        private final String name;
        private Component displayName;
        private Component prefix = Component.empty();
        private Component suffix = Component.empty();
        private NamedTextColor color;
        private boolean friendlyFire = true;
        private boolean seeFriendlyInvisibles = true;
        private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
        private CollisionRule collisionRule = CollisionRule.ALWAYS;

        private Builder(@NotNull String name) {
            this.name = name;
            this.displayName = Component.text(name);
        }

        public @NotNull Builder setDisplayName(@NotNull Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public @NotNull Builder setPrefix(@NotNull Component prefix) {
            this.prefix = prefix;
            return this;
        }

        public @NotNull Builder setSuffix(@NotNull Component suffix) {
            this.suffix = suffix;
            return this;
        }

        public @NotNull Builder setColor(@Nullable NamedTextColor color) {
            this.color = color;
            return this;
        }

        public @NotNull Builder setFriendlyFire(boolean friendlyFire) {
            this.friendlyFire = friendlyFire;
            return this;
        }

        public @NotNull Builder setSeeFriendlyInvisibles(boolean seeFriendlyInvisibles) {
            this.seeFriendlyInvisibles = seeFriendlyInvisibles;
            return this;
        }

        public @NotNull Builder setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            return this;
        }

        public @NotNull Builder setCollisionRule(@NotNull CollisionRule collisionRule) {
            this.collisionRule = collisionRule;
            return this;
        }

        public @NotNull TeamInfo build() {
            return new TeamInfo(name, displayName, prefix, suffix, color, friendlyFire, seeFriendlyInvisibles,
                nameTagVisibility, collisionRule);
        }

    }

}
