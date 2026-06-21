package dev.tommyjs.craftreel.record.util;

import dev.tommyjs.craftreel.protocol.block.BlockState;

public class BlockUtil {

    public static BlockState adaptBlockState(dev.tommyjs.dynworld.block.BlockState state) {
        return BlockState.of(state.getId(), state.getData());
    }

}
