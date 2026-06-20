package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.base.ViewerContexts;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ContextGroup;
import dev.tommyjs.craftreel.replay.reference.ViewerGroup;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.reel.scene.SceneResourceKey;
import dev.tommyjs.reel.track.TrackHandle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class ContextGroupActor<M> extends AbstractActor implements ContextGroup {

    private final TrackHandle<M, Void> metaTrack;
    private final Function<M, Identifier> idResolver;
    private final SceneResourceKey<Identifier, ContextGroup> key;
    private final ViewerGroup group = new ViewerGroup();

    private Identifier id;
    private boolean published;

    public ContextGroupActor(@NotNull TrackHandle<M, Void> metaTrack, @NotNull Function<M, Identifier> idResolver,
                             @NotNull SceneResourceKey<Identifier, ContextGroup> key) {
        this.metaTrack = metaTrack;
        this.idResolver = idResolver;
        this.key = key;
    }

    @Override
    protected void configure() {
        onCreate(metaTrack, meta -> {
            id = idResolver.apply(meta);
            scene.getResourceManager().publish(key, id, this);
            published = true;
        });

        onDestroy(() -> {
            if (published) {
                for (SpectatorRegistry registry : scene.getResourceManager().getResources(BaseResources.SPECTATORS)) {
                    registry.purge(this);
                }
                scene.getResourceManager().unpublish(key, id);
            }
            group.clear();
        });
    }

    @Override
    public @NotNull Identifier id() {
        return id;
    }

    @Override
    public @NotNull ViewerGroup group() {
        return group;
    }

    @Override
    public void addViewer(@NotNull Player player) {
        ViewerContexts.makeExclusive(scene.getResourceManager(), key, this, player);
        group.addViewer(player);
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        group.removeViewer(player);
    }

}
