package dev.tommyjs.craftreel.protocol.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public sealed interface SidebarDelta
    permits SidebarDelta.SetLine, SidebarDelta.AddLine, SidebarDelta.RemoveLine {

    record SetLine(int index, @NotNull Component before, @NotNull Component after) implements SidebarDelta {
    }

    record AddLine(int index, @NotNull Component text) implements SidebarDelta {
    }

    record RemoveLine(int index, @NotNull Component text) implements SidebarDelta {
    }

}
