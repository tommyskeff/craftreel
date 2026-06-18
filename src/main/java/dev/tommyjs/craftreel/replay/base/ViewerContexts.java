package dev.tommyjs.craftreel.replay.base;

import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.scene.SceneResourceKey;
import dev.tommyjs.reel.scene.SceneResourceManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ViewerContexts {

    private ViewerContexts() {
    }

    public static <V extends ViewerContext> void makeExclusive(@NotNull SceneResourceManager resources,
                                                               @NotNull SceneResourceKey<Identifier, V> key,
                                                               @NotNull V self, @NotNull Player player) {
        for (V other : resources.getResources(key)) {
            if (other != self) {
                other.removeViewer(player);
            }
        }
    }

}
