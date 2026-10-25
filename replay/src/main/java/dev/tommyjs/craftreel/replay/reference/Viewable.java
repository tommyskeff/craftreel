package dev.tommyjs.craftreel.replay.reference;

import dev.tommyjs.craftreel.replay.ViewerHandover;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Viewable {

    void addViewer(@NotNull Player player);

    void removeViewer(@NotNull Player player);

    default void handOff(@NotNull Player player, @NotNull ViewerHandover handover) {
        removeViewer(player);
    }

    default void takeOver(@NotNull Player player, @NotNull ViewerHandover handover) {
        addViewer(player);
    }

}
