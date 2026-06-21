package dev.tommyjs.craftreel.protocol.block;

import org.jetbrains.annotations.NotNull;

public record BlockState(int id, int data) {

    public static final BlockState AIR = new BlockState(0, 0);

    public BlockState {
        id &= 0x0FFF;
        data &= 0x000F;
    }

    public static @NotNull BlockState of(int id, int data) {
        return new BlockState(id, data);
    }

    public static @NotNull BlockState of(int fullId) {
        return new BlockState(fullId & 0x0FFF, (fullId >> 12) & 0x000F);
    }

    public int getFullId() {
        return id | (data << 12);
    }

    public boolean isAir() {
        return id == 0;
    }

}
