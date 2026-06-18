package dev.tommyjs.craftreel.protocol.scoreboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ObjectiveInfo(@NotNull Component displayName, @NotNull ScoreboardRenderType renderType,
                            @Nullable Integer slot) {
}
