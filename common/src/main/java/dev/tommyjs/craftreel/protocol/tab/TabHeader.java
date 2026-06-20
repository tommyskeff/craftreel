package dev.tommyjs.craftreel.protocol.tab;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record TabHeader(@NotNull Component header, @NotNull Component footer) {
}
