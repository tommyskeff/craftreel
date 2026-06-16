package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityMetadata {

    private final Map<Integer, EntityMetadataValue> values;

    public EntityMetadata(@NotNull Map<Integer, EntityMetadataValue> values) {
        this.values = new LinkedHashMap<>(values);
    }

    public @NotNull Map<Integer, EntityMetadataValue> values() {
        return values;
    }

}
