package dev.tommyjs.craftreel.replay.base.handler;

import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.reel.replay.update.ReplayUpdate;
import dev.tommyjs.reel.scene.ReplayScene;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import dev.tommyjs.reel.scene.SceneTransition;
import org.jetbrains.annotations.NotNull;

public final class WorldTickHandler implements SceneHandler {

    @Override
    public void load(@NotNull SceneHandlerContext ctx) {
        ctx.registerConsumer(BaseResources.WORLD);
        ctx.registerConsumer(BaseResources.ENTITY_ID);
    }

    @Override
    public void run(@NotNull ReplayUpdate update, @NotNull SceneTransition transition, @NotNull ReplayScene scene) {
        for (WorldContext world : scene.getResourceManager().getResources(BaseResources.WORLD)) {
            world.tracker().tick();
        }
    }

    @Override
    public void stop(@NotNull ReplayScene scene) {
    }

}
