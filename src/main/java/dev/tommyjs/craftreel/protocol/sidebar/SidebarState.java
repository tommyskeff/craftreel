package dev.tommyjs.craftreel.protocol.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SidebarState(@NotNull List<Component> lines) {

    public SidebarState(@NotNull List<Component> lines) {
        this.lines = new ArrayList<>(lines);
    }

}
