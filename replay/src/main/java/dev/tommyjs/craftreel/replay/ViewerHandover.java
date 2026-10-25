package dev.tommyjs.craftreel.replay;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ViewerHandover {

    private final Player player;
    private final ViewerWorldChange worldChange;
    private final Map<Object, Held> held = new LinkedHashMap<>();

    ViewerHandover(@NotNull Player player, @NotNull ViewerWorldChange worldChange) {
        this.player = player;
        this.worldChange = worldChange;
    }

    public @NotNull Player player() {
        return player;
    }

    public void hold(@NotNull Object key, @NotNull Object value, @Nullable Runnable release) {
        Held previous = held.put(key, new Held(value, release));
        if (previous != null && previous.release() != null) {
            previous.release().run();
        }
    }

    public boolean claim(@NotNull Object key) {
        return held.remove(key) != null;
    }

    public <T> @Nullable T take(@NotNull Object key, @NotNull Class<T> type) {
        Held entry = held.get(key);
        if (entry == null || !type.isInstance(entry.value())) {
            return null;
        }
        held.remove(key);
        return type.cast(entry.value());
    }

    public void changeWorld(@NotNull Location to) {
        worldChange.change(player, to);
    }

    void finish() {
        List<Held> remaining = new ArrayList<>(held.values());
        held.clear();
        for (Held entry : remaining) {
            if (entry.release() != null) {
                entry.release().run();
            }
        }
    }

    private record Held(Object value, Runnable release) {
    }

}
