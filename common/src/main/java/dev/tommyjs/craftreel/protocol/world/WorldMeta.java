package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.util.Vec3;
import org.jetbrains.annotations.NotNull;

public record WorldMeta(@NotNull Identifier id, @NotNull Environment environment, @NotNull Vec3 spawnLocation) {
}
