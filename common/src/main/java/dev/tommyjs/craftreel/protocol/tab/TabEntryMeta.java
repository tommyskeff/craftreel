package dev.tommyjs.craftreel.protocol.tab;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record TabEntryMeta(@NotNull Identifier contextId, @NotNull UUID profileId, @NotNull String name,
                           @Nullable String skinValue, @Nullable String skinSignature) {
}
