package dev.tommyjs.craftreel.protocol.sidebar;

import dev.tommyjs.reel.track.TrackModel;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SidebarModel implements TrackModel<SidebarState, SidebarDelta> {

    @Override
    public @NotNull SidebarState applyDelta(@NotNull SidebarState state, @NotNull SidebarDelta delta) {
        List<Component> lines = state.lines();
        if (delta instanceof SidebarDelta.SetLine set) {
            lines.set(set.index(), set.after());
        } else if (delta instanceof SidebarDelta.AddLine add) {
            lines.add(add.index(), add.text());
        } else if (delta instanceof SidebarDelta.RemoveLine remove) {
            lines.remove(remove.index());
        } else {
            throw new IllegalStateException("Unknown sidebar delta: " + delta);
        }
        return state;
    }

    @Override
    public @NotNull SidebarState cloneState(@NotNull SidebarState state) {
        return new SidebarState(state.lines());
    }

    @Override
    public @NotNull List<SidebarDelta> condense(@NotNull SidebarState ref, @NotNull List<SidebarDelta> deltas) {
        return List.copyOf(deltas);
    }

    @Override
    public @NotNull SidebarDelta reverse(@NotNull SidebarDelta delta) {
        if (delta instanceof SidebarDelta.SetLine set) {
            return new SidebarDelta.SetLine(set.index(), set.after(), set.before());
        } else if (delta instanceof SidebarDelta.AddLine add) {
            return new SidebarDelta.RemoveLine(add.index(), add.text());
        } else if (delta instanceof SidebarDelta.RemoveLine remove) {
            return new SidebarDelta.AddLine(remove.index(), remove.text());
        } else {
            throw new IllegalStateException("Unknown sidebar delta: " + delta);
        }
    }

}
