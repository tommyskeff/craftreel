package dev.tommyjs.craftreel.replay;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.replay.reference.ContextSlot;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.reel.scene.SceneResourceKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ReplaySpectator {

    @NotNull Player player();

    void attach(@NotNull ViewerContext context);

    void detach(@NotNull ViewerContext context);

    <C extends ViewerContext> void assign(@NotNull ContextSlot<C> slot, @NotNull C context);

    <C extends ViewerContext> void clear(@NotNull ContextSlot<C> slot);

    <C extends ViewerContext> @Nullable C get(@NotNull ContextSlot<C> slot);

    @NotNull Collection<ViewerContext> attached();

    <C extends ViewerContext> void assign(@NotNull ContextSlot<C> slot,
                                          @NotNull SceneResourceKey<Identifier, C> key, @NotNull Identifier id);

    void attach(@NotNull SceneResourceKey<Identifier, ? extends ViewerContext> key, @NotNull Identifier id);

}
