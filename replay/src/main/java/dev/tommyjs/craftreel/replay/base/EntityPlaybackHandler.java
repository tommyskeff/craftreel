package dev.tommyjs.craftreel.replay.base;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol.Entities;
import dev.tommyjs.craftreel.replay.base.actor.EntityPlaybackConfig;
import dev.tommyjs.craftreel.replay.base.actor.MobActor;
import dev.tommyjs.craftreel.replay.base.actor.PlayerActor;
import dev.tommyjs.reel.replay.update.ReplayUpdate;
import dev.tommyjs.reel.scene.ActorHandler;
import dev.tommyjs.reel.scene.ReplayScene;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import dev.tommyjs.reel.scene.SceneTransition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EntityPlaybackHandler implements SceneHandler {

    private final List<SceneHandler> delegates;

    public EntityPlaybackHandler() {
        this(EntityPlaybackConfig.DEFAULT);
    }

    public EntityPlaybackHandler(@NotNull EntityPlaybackConfig config) {
        this.delegates = List.of(
            ActorHandler.create(Entities.ENTITY, () -> new MobActor(config))
                .consumes(BaseResources.WORLD).provides(BaseResources.ENTITY_ID),
            ActorHandler.create(Entities.PLAYER, () -> new PlayerActor(config))
                .consumes(BaseResources.WORLD).provides(BaseResources.ENTITY_ID)
        );
    }

    @Override
    public void load(@NotNull SceneHandlerContext ctx) {
        for (SceneHandler delegate : delegates) {
            delegate.load(ctx);
        }
    }

    @Override
    public void run(@NotNull ReplayUpdate update, @NotNull SceneTransition transition, @NotNull ReplayScene scene) {
        for (SceneHandler delegate : delegates) {
            delegate.run(update, transition, scene);
        }
    }

    @Override
    public void stop(@NotNull ReplayScene scene) {
        for (SceneHandler delegate : delegates) {
            delegate.stop(scene);
        }
    }

}
