package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record EntityPotionEffectState(@NotNull List<EntityPotionEffect> effects) {
}
