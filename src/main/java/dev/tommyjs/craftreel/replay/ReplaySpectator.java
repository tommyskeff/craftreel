package dev.tommyjs.craftreel.replay;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.reel.scene.SceneResourceKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ReplaySpectator {

    @NotNull Player player();

    void attach(@NotNull ViewerContext context);

    void detach(@NotNull ViewerContext context);

    @NotNull Collection<ViewerContext> attached();

    void attach(@NotNull SceneResourceKey<Identifier, ? extends ViewerContext> key, @NotNull Identifier id);

}
