package dev.tommyjs.craftreel.protocol.world;

import dev.tommyjs.craftreel.util.Identifier;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public record WorldMeta(@NotNull Identifier id, @NotNull World.Environment environment, @NotNull Vector spawnLocation) {
}
