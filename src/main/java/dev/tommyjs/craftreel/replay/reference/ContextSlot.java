package dev.tommyjs.craftreel.replay.reference;

import org.jetbrains.annotations.NotNull;

public final class ContextSlot<C extends ViewerContext> {

    private final String name;

    private ContextSlot(@NotNull String name) {
        this.name = name;
    }

    public static <C extends ViewerContext> @NotNull ContextSlot<C> of(@NotNull String name) {
        return new ContextSlot<>(name);
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ContextSlot[" + name + "]";
    }

}
