package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.protocol.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record EntityEquipmentDelta(int slot, @NotNull ItemStack before, @NotNull ItemStack after) {
}
