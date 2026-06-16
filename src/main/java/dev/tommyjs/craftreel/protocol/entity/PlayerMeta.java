package dev.tommyjs.craftreel.protocol.entity;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayerMeta(@NotNull UUID id, @NotNull Identifier worldId, @NotNull UUID profileId, @NotNull String name,
                         @Nullable String skinValue, @Nullable String skinSignature) {
}
