package dev.tommyjs.craftreel.protocol;

import dev.tommyjs.craftreel.protocol.chat.ChatLine;
import dev.tommyjs.craftreel.protocol.chat.ChatLineCodec;
import dev.tommyjs.craftreel.protocol.chat.ChatLineModel;
import dev.tommyjs.craftreel.protocol.entity.*;
import dev.tommyjs.craftreel.protocol.text.TextContext;
import dev.tommyjs.craftreel.protocol.text.TextContextCodec;
import dev.tommyjs.craftreel.protocol.text.TextContextModel;
import dev.tommyjs.craftreel.protocol.title.Title;
import dev.tommyjs.craftreel.protocol.title.TitleCodec;
import dev.tommyjs.craftreel.protocol.title.TitleModel;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContent;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentDelta;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentDeltaCodec;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentModel;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionContentStateCodec;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionMeta;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionMetaCodec;
import dev.tommyjs.craftreel.protocol.chunk.ChunkSectionMetaModel;
import dev.tommyjs.craftreel.protocol.block.BlockBreakProgress;
import dev.tommyjs.craftreel.protocol.block.BlockBreakProgressCodec;
import dev.tommyjs.craftreel.protocol.block.BlockBreakProgressModel;
import dev.tommyjs.craftreel.protocol.world.ExplosionEvent;
import dev.tommyjs.craftreel.protocol.world.ExplosionEventCodec;
import dev.tommyjs.craftreel.protocol.world.ExplosionEventModel;
import dev.tommyjs.craftreel.protocol.world.WorldEvent;
import dev.tommyjs.craftreel.protocol.world.WorldEventCodec;
import dev.tommyjs.craftreel.protocol.world.WorldEventModel;
import dev.tommyjs.craftreel.protocol.world.ParticleEvent;
import dev.tommyjs.craftreel.protocol.world.ParticleEventCodec;
import dev.tommyjs.craftreel.protocol.world.ParticleEventModel;
import dev.tommyjs.craftreel.protocol.world.SoundEvent;
import dev.tommyjs.craftreel.protocol.world.SoundEventCodec;
import dev.tommyjs.craftreel.protocol.world.SoundEventModel;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarDelta;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarDeltaCodec;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarMeta;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarMetaCodec;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarMetaModel;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarModel;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarState;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarStateCodec;
import dev.tommyjs.craftreel.protocol.tab.TabHeader;
import dev.tommyjs.craftreel.protocol.tab.TabHeaderCodec;
import dev.tommyjs.craftreel.protocol.tab.TabHeaderModel;
import dev.tommyjs.craftreel.protocol.tab.TabHeaderMeta;
import dev.tommyjs.craftreel.protocol.tab.TabHeaderMetaCodec;
import dev.tommyjs.craftreel.protocol.tab.TabHeaderMetaModel;
import dev.tommyjs.craftreel.protocol.tab.TabListMeta;
import dev.tommyjs.craftreel.protocol.tab.TabListMetaCodec;
import dev.tommyjs.craftreel.protocol.tab.TabListMetaModel;
import dev.tommyjs.craftreel.protocol.tab.TabEntryMeta;
import dev.tommyjs.craftreel.protocol.tab.TabEntryMetaCodec;
import dev.tommyjs.craftreel.protocol.tab.TabEntryMetaModel;
import dev.tommyjs.craftreel.protocol.tab.TabEntryState;
import dev.tommyjs.craftreel.protocol.tab.TabEntryStateCodec;
import dev.tommyjs.craftreel.protocol.tab.TabEntryStateModel;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardMeta;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardMetaCodec;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardMetaModel;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveMeta;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveMetaCodec;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveMetaModel;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveInfo;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveInfoCodec;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveInfoModel;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScores;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScoresCodec;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScoresModel;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScoreDelta;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveScoreDeltaCodec;
import dev.tommyjs.craftreel.protocol.team.TeamsMeta;
import dev.tommyjs.craftreel.protocol.team.TeamsMetaCodec;
import dev.tommyjs.craftreel.protocol.team.TeamsMetaModel;
import dev.tommyjs.craftreel.protocol.team.TeamMeta;
import dev.tommyjs.craftreel.protocol.team.TeamMetaCodec;
import dev.tommyjs.craftreel.protocol.team.TeamMetaModel;
import dev.tommyjs.craftreel.protocol.team.TeamInfo;
import dev.tommyjs.craftreel.protocol.team.TeamInfoCodec;
import dev.tommyjs.craftreel.protocol.team.TeamInfoModel;
import dev.tommyjs.craftreel.protocol.team.TeamMembers;
import dev.tommyjs.craftreel.protocol.team.TeamMembersCodec;
import dev.tommyjs.craftreel.protocol.team.TeamMembersDelta;
import dev.tommyjs.craftreel.protocol.team.TeamMembersDeltaCodec;
import dev.tommyjs.craftreel.protocol.team.TeamMembersModel;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.world.EnvironmentState;
import dev.tommyjs.craftreel.protocol.world.EnvironmentStateCodec;
import dev.tommyjs.craftreel.protocol.world.EnvironmentStateModel;
import dev.tommyjs.craftreel.protocol.world.WorldMeta;
import dev.tommyjs.craftreel.protocol.world.WorldMetaCodec;
import dev.tommyjs.craftreel.protocol.world.WorldMetaModel;
import dev.tommyjs.reel.track.EntityTypeHandle;
import dev.tommyjs.reel.track.TrackHandle;
import dev.tommyjs.reel.track.TrackRegistry;

public final class CraftReelProtocol {

    public static final String NAMESPACE = "craftreel";

    private CraftReelProtocol() {
    }

    private static String namespaced(String category, String name) {
        return NAMESPACE + ":" + category + "/" + name;
    }

    private static String entity(String name) {
        return namespaced("entity", name);
    }

    private static String track(String name) {
        return namespaced("track", name);
    }

    public static final class Entities {

        public static final EntityTypeHandle WORLD = EntityTypeHandle.of(entity("world"));
        public static final EntityTypeHandle CHUNK_SECTION = EntityTypeHandle.of(entity("chunk_section"));
        public static final EntityTypeHandle TEXT = EntityTypeHandle.of(entity("text"));
        public static final EntityTypeHandle SIDEBAR = EntityTypeHandle.of(entity("sidebar"));
        public static final EntityTypeHandle TAB_HEADER = EntityTypeHandle.of(entity("tab_header"));
        public static final EntityTypeHandle TAB_LIST = EntityTypeHandle.of(entity("tab_list"));
        public static final EntityTypeHandle TAB_ENTRY = EntityTypeHandle.of(entity("tab_entry"));
        public static final EntityTypeHandle SCOREBOARD = EntityTypeHandle.of(entity("scoreboard"));
        public static final EntityTypeHandle OBJECTIVE = EntityTypeHandle.of(entity("objective"));
        public static final EntityTypeHandle TEAMS = EntityTypeHandle.of(entity("teams"));
        public static final EntityTypeHandle TEAM = EntityTypeHandle.of(entity("team"));
        public static final EntityTypeHandle ENTITY = EntityTypeHandle.of(entity("entity"));
        public static final EntityTypeHandle PLAYER = EntityTypeHandle.of(entity("player"));

        private Entities() {
        }

    }

    public static final class Tracks {

        public static final TrackHandle<WorldMeta, Void> WORLD_META =
            TrackHandle.of(track("world_meta"), WorldMeta.class, Void.class);

        public static final TrackHandle<EnvironmentState, Void> WORLD_ENVIRONMENT =
            TrackHandle.of(track("world_environment"), EnvironmentState.class, Void.class);

        public static final TrackHandle<ChunkSectionMeta, Void> CHUNK_SECTION_META =
            TrackHandle.of(track("chunk_section_meta"), ChunkSectionMeta.class, Void.class);

        public static final TrackHandle<ChunkSectionContent, ChunkSectionContentDelta> CHUNK_SECTION_CONTENT =
            TrackHandle.of(track("chunk_section_content"), ChunkSectionContent.class, ChunkSectionContentDelta.class);

        public static final TrackHandle<TextContext, Void> TEXT_META =
            TrackHandle.of(track("text_meta"), TextContext.class, Void.class);

        public static final TrackHandle<SidebarMeta, Void> SIDEBAR_META =
            TrackHandle.of(track("sidebar_meta"), SidebarMeta.class, Void.class);

        public static final TrackHandle<SidebarState, SidebarDelta> SIDEBAR =
            TrackHandle.of(track("sidebar"), SidebarState.class, SidebarDelta.class);

        public static final TrackHandle<TabHeaderMeta, Void> TAB_HEADER_META =
            TrackHandle.of(track("tab_header_meta"), TabHeaderMeta.class, Void.class);

        public static final TrackHandle<TabHeader, Void> TAB_HEADER =
            TrackHandle.of(track("tab_header"), TabHeader.class, Void.class);

        public static final TrackHandle<TabListMeta, Void> TAB_LIST_META =
            TrackHandle.of(track("tab_list_meta"), TabListMeta.class, Void.class);

        public static final TrackHandle<TabEntryMeta, Void> TAB_ENTRY_META =
            TrackHandle.of(track("tab_entry_meta"), TabEntryMeta.class, Void.class);

        public static final TrackHandle<TabEntryState, Void> TAB_ENTRY_STATE =
            TrackHandle.of(track("tab_entry_state"), TabEntryState.class, Void.class);

        public static final TrackHandle<ScoreboardMeta, Void> SCOREBOARD_META =
            TrackHandle.of(track("scoreboard_meta"), ScoreboardMeta.class, Void.class);

        public static final TrackHandle<ObjectiveMeta, Void> OBJECTIVE_META =
            TrackHandle.of(track("objective_meta"), ObjectiveMeta.class, Void.class);

        public static final TrackHandle<ObjectiveInfo, Void> OBJECTIVE_INFO =
            TrackHandle.of(track("objective_info"), ObjectiveInfo.class, Void.class);

        public static final TrackHandle<ObjectiveScores, ObjectiveScoreDelta> OBJECTIVE_SCORES =
            TrackHandle.of(track("objective_scores"), ObjectiveScores.class, ObjectiveScoreDelta.class);

        public static final TrackHandle<TeamsMeta, Void> TEAMS_META =
            TrackHandle.of(track("teams_meta"), TeamsMeta.class, Void.class);

        public static final TrackHandle<TeamMeta, Void> TEAM_META =
            TrackHandle.of(track("team_meta"), TeamMeta.class, Void.class);

        public static final TrackHandle<TeamInfo, Void> TEAM_INFO =
            TrackHandle.of(track("team_info"), TeamInfo.class, Void.class);

        public static final TrackHandle<TeamMembers, TeamMembersDelta> TEAM_MEMBERS =
            TrackHandle.of(track("team_members"), TeamMembers.class, TeamMembersDelta.class);

        public static final TrackHandle<EntityMeta, Void> ENTITY_META =
            TrackHandle.of(track("entity_meta"), EntityMeta.class, Void.class);

        public static final TrackHandle<PlayerMeta, Void> PLAYER_META =
            TrackHandle.of(track("player_meta"), PlayerMeta.class, Void.class);

        public static final TrackHandle<EntityPose, EntityPoseDelta> ENTITY_POSE =
            TrackHandle.of(track("entity_pose"), EntityPose.class, EntityPoseDelta.class);

        public static final TrackHandle<EntityMetadata, EntityMetadataDelta> ENTITY_METADATA =
            TrackHandle.of(track("entity_metadata"), EntityMetadata.class, EntityMetadataDelta.class);

        public static final TrackHandle<EntityEquipment, EntityEquipmentDelta> ENTITY_EQUIPMENT =
            TrackHandle.of(track("entity_equipment"), EntityEquipment.class, EntityEquipmentDelta.class);

        public static final TrackHandle<Void, EntityAnimationType> ENTITY_ANIMATION =
            TrackHandle.of(track("entity_animation"), Void.class, EntityAnimationType.class);

        public static final TrackHandle<Void, EntityVelocity> ENTITY_VELOCITY =
            TrackHandle.of(track("entity_velocity"), Void.class, EntityVelocity.class);

        public static final TrackHandle<EntityPresence, Void> ENTITY_PRESENCE =
            TrackHandle.of(track("entity_presence"), EntityPresence.class, Void.class);

        public static final TrackHandle<EntityPotionEffectState, Void> ENTITY_POTION =
            TrackHandle.of(track("entity_potion"), EntityPotionEffectState.class, Void.class);

        public static final TrackHandle<Void, SoundEvent> SOUND =
            TrackHandle.of(track("sound"), Void.class, SoundEvent.class);

        public static final TrackHandle<Void, ParticleEvent> PARTICLE =
            TrackHandle.of(track("particle"), Void.class, ParticleEvent.class);

        public static final TrackHandle<Void, ExplosionEvent> EXPLOSION =
            TrackHandle.of(track("explosion"), Void.class, ExplosionEvent.class);

        public static final TrackHandle<Void, WorldEvent> WORLD_EVENT =
            TrackHandle.of(track("world_event"), Void.class, WorldEvent.class);

        public static final TrackHandle<Void, BlockBreakProgress> BLOCK_BREAK =
            TrackHandle.of(track("block_break"), Void.class, BlockBreakProgress.class);

        public static final TrackHandle<Void, ChatLine> CHAT_MESSAGE =
            TrackHandle.of(track("chat_message"), Void.class, ChatLine.class);

        public static final TrackHandle<Void, Title> TITLE =
            TrackHandle.of(track("title"), Void.class, Title.class);

        private Tracks() {
        }

    }

    public static final class Defaults {

        public static final Identifier TEXT = Identifier.of(NAMESPACE, "text/default");
        public static final Identifier SIDEBAR = Identifier.of(NAMESPACE, "sidebar/default");
        public static final Identifier TAB_HEADER = Identifier.of(NAMESPACE, "tab/default");
        public static final Identifier TAB_LIST = Identifier.of(NAMESPACE, "tab/list");
        public static final Identifier SCOREBOARD = Identifier.of(NAMESPACE, "scoreboard/default");
        public static final Identifier TEAM = Identifier.of(NAMESPACE, "team/default");

        private Defaults() {
        }

    }

    public static TrackRegistry.Builder defaultTrackRegistry() {
        return TrackRegistry.builder()
            .register(Tracks.WORLD_META, new WorldMetaModel(), new WorldMetaCodec())
            .register(Tracks.CHUNK_SECTION_META, new ChunkSectionMetaModel(), new ChunkSectionMetaCodec())
            .register(Tracks.CHUNK_SECTION_CONTENT, new ChunkSectionContentModel(), new ChunkSectionContentStateCodec(), new ChunkSectionContentDeltaCodec())
            .register(Tracks.TEXT_META, new TextContextModel(), new TextContextCodec())
            .register(Tracks.SIDEBAR_META, new SidebarMetaModel(), new SidebarMetaCodec())
            .register(Tracks.SIDEBAR, new SidebarModel(), new SidebarStateCodec(), new SidebarDeltaCodec())
            .register(Tracks.TAB_HEADER_META, new TabHeaderMetaModel(), new TabHeaderMetaCodec())
            .register(Tracks.TAB_HEADER, new TabHeaderModel(), new TabHeaderCodec())
            .register(Tracks.TAB_LIST_META, new TabListMetaModel(), new TabListMetaCodec())
            .register(Tracks.TAB_ENTRY_META, new TabEntryMetaModel(), new TabEntryMetaCodec())
            .register(Tracks.TAB_ENTRY_STATE, new TabEntryStateModel(), new TabEntryStateCodec())
            .register(Tracks.SCOREBOARD_META, new ScoreboardMetaModel(), new ScoreboardMetaCodec())
            .register(Tracks.OBJECTIVE_META, new ObjectiveMetaModel(), new ObjectiveMetaCodec())
            .register(Tracks.OBJECTIVE_INFO, new ObjectiveInfoModel(), new ObjectiveInfoCodec())
            .register(Tracks.OBJECTIVE_SCORES, new ObjectiveScoresModel(), new ObjectiveScoresCodec(), new ObjectiveScoreDeltaCodec())
            .register(Tracks.TEAMS_META, new TeamsMetaModel(), new TeamsMetaCodec())
            .register(Tracks.TEAM_META, new TeamMetaModel(), new TeamMetaCodec())
            .register(Tracks.TEAM_INFO, new TeamInfoModel(), new TeamInfoCodec())
            .register(Tracks.TEAM_MEMBERS, new TeamMembersModel(), new TeamMembersCodec(), new TeamMembersDeltaCodec())
            .register(Tracks.ENTITY_META, new EntityMetaModel(), new EntityMetaCodec())
            .register(Tracks.PLAYER_META, new PlayerMetaModel(), new PlayerMetaCodec())
            .register(Tracks.ENTITY_POSE, new EntityPoseModel(), new EntityPoseStateCodec(), new EntityPoseDeltaCodec())
            .register(Tracks.ENTITY_METADATA, new EntityMetadataModel(), new EntityMetadataStateCodec(), new EntityMetadataDeltaCodec())
            .register(Tracks.ENTITY_EQUIPMENT, new EntityEquipmentModel(), new EntityEquipmentStateCodec(), new EntityEquipmentDeltaCodec())
            .register(Tracks.ENTITY_ANIMATION, new EntityAnimationModel(), new EntityAnimationCodec())
            .register(Tracks.ENTITY_VELOCITY, new EntityVelocityModel(), new EntityVelocityCodec())
            .register(Tracks.ENTITY_PRESENCE, new EntityPresenceModel(), new EntityPresenceStateCodec())
            .register(Tracks.ENTITY_POTION, new EntityPotionEffectStateModel(), new EntityPotionEffectStateCodec())
            .register(Tracks.WORLD_ENVIRONMENT, new EnvironmentStateModel(), new EnvironmentStateCodec())
            .register(Tracks.SOUND, new SoundEventModel(), new SoundEventCodec())
            .register(Tracks.PARTICLE, new ParticleEventModel(), new ParticleEventCodec())
            .register(Tracks.EXPLOSION, new ExplosionEventModel(), new ExplosionEventCodec())
            .register(Tracks.WORLD_EVENT, new WorldEventModel(), new WorldEventCodec())
            .register(Tracks.BLOCK_BREAK, new BlockBreakProgressModel(), new BlockBreakProgressCodec())
            .register(Tracks.CHAT_MESSAGE, new ChatLineModel(), new ChatLineCodec())
            .register(Tracks.TITLE, new TitleModel(), new TitleCodec());
    }

}
