package dev.tommyjs.craftreel.replay.reference;

import dev.tommyjs.craftreel.util.Identifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ViewerContext {

    @NotNull Identifier id();

    void attach(@NotNull Player player);

    void detach(@NotNull Player player);

}
