package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.protocol.block.BlockState;
import dev.tommyjs.craftreel.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ObjectData {

    public static final int ITEM_FRAME_WIDTH_PIXELS = 12;
    public static final int ITEM_FRAME_HEIGHT_PIXELS = 12;

    private static final int HANGING_RECORDED = 1 << 30;
    private static final double HANGING_INSET = 0.46875;

    private ObjectData() {
    }

    public static int fallingBlock(@NotNull BlockState state) {
        return state.getFullId();
    }

    public static @Nullable BlockState fallingBlockState(int objectData) {
        BlockState state = BlockState.of(objectData);
        return state.isAir() ? null : state;
    }

    public static int itemFrame(@NotNull HangingFacing facing) {
        return HANGING_RECORDED | facing.horizontalIndex();
    }

    public static int painting(@NotNull PaintingArt art, @NotNull HangingFacing facing) {
        return HANGING_RECORDED | art.id() << 2 | facing.horizontalIndex();
    }

    public static boolean hasHanging(int objectData) {
        return (objectData & HANGING_RECORDED) != 0;
    }

    public static @NotNull HangingFacing hangingFacing(int objectData) {
        return HangingFacing.ofHorizontalIndex(objectData);
    }

    public static @Nullable PaintingArt paintingArt(int objectData) {
        return PaintingArt.byId((objectData & ~HANGING_RECORDED) >>> 2);
    }

    public static @NotNull Vec3 hangingBlock(double x, double y, double z, @NotNull HangingFacing facing, int widthPixels,
                                             int heightPixels) {
        HangingFacing along = facing.rotateCounterClockwise();
        double widthOffset = widthPixels % 32 == 0 ? 0.5 : 0.0;
        double heightOffset = heightPixels % 32 == 0 ? 0.5 : 0.0;
        double blockX = Math.round(x - 0.5 + facing.offsetX() * HANGING_INSET - along.offsetX() * widthOffset);
        double blockY = Math.round(y - 0.5 - heightOffset);
        double blockZ = Math.round(z - 0.5 + facing.offsetZ() * HANGING_INSET - along.offsetZ() * widthOffset);
        return new Vec3(blockX, blockY, blockZ);
    }

}
