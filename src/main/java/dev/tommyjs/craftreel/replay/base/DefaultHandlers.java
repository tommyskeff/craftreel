package dev.tommyjs.craftreel.replay.base;

import dev.tommyjs.craftreel.replay.base.actor.*;
import dev.tommyjs.craftreel.replay.base.handler.WorldTickHandler;
import dev.tommyjs.craftreel.replay.base.handler.WorldFreezeHandler;
import dev.tommyjs.craftreel.replay.base.handler.DefaultContextSpectatorHandler;
import dev.tommyjs.craftreel.replay.base.handler.SpectatorWorldSelectionHandler;
import dev.tommyjs.craftreel.replay.base.handler.SpectatorProtectionHandler;
import dev.tommyjs.craftreel.replay.base.handler.SpectatorRegistryHandler;
import dev.tommyjs.craftreel.replay.base.handler.controls.ReplayControlsHandler;
import dev.tommyjs.reel.scene.ActorHandler;
import dev.tommyjs.reel.scene.SceneHandler;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol.Entities;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol.Tracks;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardMeta;
import dev.tommyjs.craftreel.protocol.team.TeamsMeta;

import java.util.List;

public final class DefaultHandlers {

    public static final List<SceneHandler> DEFAULT_HANDLERS = List.of(
        ActorHandler.create(Entities.WORLD, WorldActor::new).provides(BaseResources.WORLD),
        ActorHandler.create(Entities.CHUNK_SECTION, ChunkSectionActor::new).consumes(BaseResources.WORLD),
        ActorHandler.create(Entities.SIDEBAR, SidebarActor::new).provides(BaseResources.SIDEBAR),
        ActorHandler.create(Entities.TAB_HEADER, TabHeaderActor::new).provides(BaseResources.TAB_HEADER),
        ActorHandler.create(Entities.SCOREBOARD, () -> new ContextGroupActor<>(
            Tracks.SCOREBOARD_META, ScoreboardMeta::id, BaseResources.SCOREBOARD)).provides(BaseResources.SCOREBOARD),
        ActorHandler.create(Entities.OBJECTIVE, ObjectiveActor::new).consumes(BaseResources.SCOREBOARD),
        ActorHandler.create(Entities.TEAMS, () -> new ContextGroupActor<>(
            Tracks.TEAMS_META, TeamsMeta::id, BaseResources.TEAM)).provides(BaseResources.TEAM),
        ActorHandler.create(Entities.TEAM, TeamActor::new).consumes(BaseResources.TEAM),
        ActorHandler.create(Entities.ENTITY, MobActor::new).consumes(BaseResources.WORLD).provides(BaseResources.ENTITY_ID),
        ActorHandler.create(Entities.PLAYER, PlayerActor::new).consumes(BaseResources.WORLD).provides(BaseResources.ENTITY_ID),
        ActorHandler.create(Entities.TEXT, ChatActor::new).provides(BaseResources.CHAT),
        ActorHandler.create(Entities.TEXT, TitleActor::new).provides(BaseResources.TITLE),
        ActorHandler.create(Entities.WORLD, EffectsActor::new).consumes(BaseResources.WORLD).provides(BaseResources.EFFECTS),
        new WorldTickHandler(),
        new SpectatorRegistryHandler(),
        new WorldFreezeHandler(),
        new SpectatorWorldSelectionHandler(),
        new DefaultContextSpectatorHandler(),
        new SpectatorProtectionHandler(),
        new ReplayControlsHandler()
    );

    private DefaultHandlers() {
    }

}
