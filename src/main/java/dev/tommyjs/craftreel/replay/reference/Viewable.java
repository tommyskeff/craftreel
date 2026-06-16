package dev.tommyjs.craftreel.replay.reference;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Viewable {

    void addViewer(@NotNull Player player);

    void removeViewer(@NotNull Player player);

}
