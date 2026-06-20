package dev.tommyjs.craftreel.replay.reference;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ViewerGroup implements Viewable {

    private final ViewerSet viewers = new ViewerSet();
    private final List<Viewable> members = new ArrayList<>();

    @Override
    public void addViewer(@NotNull Player player) {
        if (viewers.add(player)) {
            for (Viewable member : List.copyOf(members)) {
                member.addViewer(player);
            }
        }
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        if (viewers.remove(player)) {
            for (Viewable member : List.copyOf(members)) {
                member.removeViewer(player);
            }
        }
    }

    public void add(@NotNull Viewable member) {
        members.add(member);
        for (Player player : viewers.online()) {
            member.addViewer(player);
        }
    }

    public void remove(@NotNull Viewable member) {
        members.remove(member);
    }

    public @NotNull List<Player> viewers() {
        return viewers.online();
    }

    public void clear() {
        viewers.clear();
        members.clear();
    }

}
