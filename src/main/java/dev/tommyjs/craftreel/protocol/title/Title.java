package dev.tommyjs.craftreel.protocol.title;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record Title(@NotNull Component title, @NotNull Component subtitle, int fadeIn, int stay, int fadeOut) {
}
