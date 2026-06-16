package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.protocol.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityEquipment {

    public static final int HELMET = 0;
    public static final int CHESTPLATE = 1;
    public static final int LEGGINGS = 2;
    public static final int BOOTS = 3;
    public static final int MAIN_HAND = 4;

    private final Map<Integer, ItemStack> items;

    public EntityEquipment(@NotNull Map<Integer, ItemStack> items) {
        this.items = new LinkedHashMap<>(items);
    }

    public @NotNull Map<Integer, ItemStack> items() {
        return items;
    }

}
