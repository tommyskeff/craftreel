package dev.tommyjs.craftreel.protocol.tab;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public record TabEntryState(int latency, int gameMode, @Nullable Component displayName) {
}
