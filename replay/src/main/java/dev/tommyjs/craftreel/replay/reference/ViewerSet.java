package dev.tommyjs.craftreel.replay.reference;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ViewerSet {

    private final Set<UUID> ids = new LinkedHashSet<>();

    public boolean add(@NotNull Player player) {
        return ids.add(player.getUniqueId());
    }

    public boolean remove(@NotNull Player player) {
        return ids.remove(player.getUniqueId());
    }

    public void clear() {
        ids.clear();
    }

    public @NotNull List<Player> online() {
        List<Player> players = new ArrayList<>(ids.size());
        ids.removeIf(id -> {
            Player player = Bukkit.getPlayer(id);
            if (player == null || !player.isOnline()) {
                return true;
            }
            players.add(player);
            return false;
        });
        return players;
    }

}
