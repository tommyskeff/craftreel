package dev.tommyjs.craftreel.replay.base;

import dev.tommyjs.craftreel.replay.ReplaySpectator;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

public final class SpectatorHelper {

    private SpectatorHelper() {
    }

    public static void setWorld(@NotNull ReplaySpectator spectator, @NotNull WorldContext world) {
        spectator.assign(BaseResources.Slots.WORLD, world);
    }

    public static void setWorld(@NotNull ReplaySpectator spectator, @NotNull Identifier id) {
        spectator.assign(BaseResources.Slots.WORLD, BaseResources.WORLD, id);
    }

    public static void setSidebar(@NotNull ReplaySpectator spectator, @NotNull ViewerContext sidebar) {
        spectator.assign(BaseResources.Slots.SIDEBAR, sidebar);
    }

    public static void setSidebar(@NotNull ReplaySpectator spectator, @NotNull Identifier id) {
        spectator.assign(BaseResources.Slots.SIDEBAR, BaseResources.SIDEBAR, id);
    }

    public static void clearSidebar(@NotNull ReplaySpectator spectator) {
        spectator.clear(BaseResources.Slots.SIDEBAR);
    }

    public static void setTabHeader(@NotNull ReplaySpectator spectator, @NotNull ViewerContext tabHeader) {
        spectator.assign(BaseResources.Slots.TAB_HEADER, tabHeader);
    }

    public static void setTabHeader(@NotNull ReplaySpectator spectator, @NotNull Identifier id) {
        spectator.assign(BaseResources.Slots.TAB_HEADER, BaseResources.TAB_HEADER, id);
    }

    public static void clearTabHeader(@NotNull ReplaySpectator spectator) {
        spectator.clear(BaseResources.Slots.TAB_HEADER);
    }

    public static void joinChat(@NotNull ReplaySpectator spectator, @NotNull ViewerContext chat) {
        spectator.attach(chat);
    }

    public static void joinChat(@NotNull ReplaySpectator spectator, @NotNull Identifier id) {
        spectator.attach(BaseResources.CHAT, id);
    }

    public static void leaveChat(@NotNull ReplaySpectator spectator, @NotNull ViewerContext chat) {
        spectator.detach(chat);
    }

}
