package dev.tommyjs.craftreel.replay;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ViewerWorldChange {

    ViewerWorldChange TELEPORT = Player::teleport;

    void change(@NotNull Player player, @NotNull Location to);

}
