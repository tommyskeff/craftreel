package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.NotNull;

public enum HangingFacing {

    SOUTH(0, 1),
    WEST(-1, 0),
    NORTH(0, -1),
    EAST(1, 0);

    private final int offsetX;
    private final int offsetZ;

    HangingFacing(int offsetX, int offsetZ) {
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public static @NotNull HangingFacing ofHorizontalIndex(int index) {
        return values()[index & 3];
    }

    public int horizontalIndex() {
        return ordinal();
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public @NotNull HangingFacing rotateCounterClockwise() {
        return values()[(ordinal() + 3) & 3];
    }

}
