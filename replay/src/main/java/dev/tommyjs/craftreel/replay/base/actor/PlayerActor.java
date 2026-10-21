package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.entity.PlayerMeta;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.ReplayPlayer;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.dynworld.entity.EntityPose;
import dev.tommyjs.dynworld.entity.PlayerEntity;
import dev.tommyjs.dynworld.entity.VirtualEntity;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PlayerActor extends AbstractTrackedActor {

    private boolean tablistOwned;
    private PlayerMeta meta;

    public PlayerActor() {
        this(EntityPlaybackConfig.DEFAULT);
    }

    public PlayerActor(EntityPlaybackConfig config) {
        super(config);
    }

    @Override
    protected void registerCreate() {
        onCreate(CraftReelProtocol.Tracks.PLAYER_META, meta -> {
            this.meta = meta;
            UUID tabEntryUuid = scene.getResourceManager().find(BaseResources.TAB_ENTRY, meta.profileId());
            this.tablistOwned = tabEntryUuid != null;
            UserProfile profile = tablistOwned
                ? new UserProfile(tabEntryUuid, meta.name())
                : createProfile(meta);
            install(meta.id(), meta.worldId(), tracker -> tracker.spawnPlayer(profile, ORIGIN));
            scene.getResourceManager().publish(BaseResources.PLAYER, meta.id(), new Navigable());
        });

        onDestroy(() -> {
            if (meta != null) {
                scene.getResourceManager().unpublish(BaseResources.PLAYER, meta.id());
            }
        });
    }

    @Override
    protected void onEntitySpawned(@NotNull VirtualEntity entity) {
        if (tablistOwned && entity instanceof PlayerEntity player) {
            player.setTablistManaged(false);
        }
    }

    private static UserProfile createProfile(PlayerMeta meta) {
        UUID profileId = UUID.randomUUID();
        if (meta.skinValue() == null) {
            return new UserProfile(profileId, meta.name());
        }

        TextureProperty texture = new TextureProperty("textures", meta.skinValue(), meta.skinSignature());
        return new UserProfile(profileId, meta.name(), List.of(texture));
    }

    private final class Navigable implements ReplayPlayer {

        @Override
        public @NotNull UUID id() {
            return meta.id();
        }

        @Override
        public int entityId() {
            return PlayerActor.this.entityId();
        }

        @Override
        public @NotNull String name() {
            return meta.name();
        }

        @Override
        public @Nullable String skinValue() {
            return meta.skinValue();
        }

        @Override
        public @Nullable String skinSignature() {
            return meta.skinSignature();
        }

        @Override
        public boolean present() {
            return PlayerActor.this.present();
        }

        @Override
        public @Nullable Location location() {
            WorldContext world = world();
            if (world == null) {
                return null;
            }

            EntityPose pose = pose();
            return new Location(world.world().getBukkitWorld(), pose.x(), pose.y(), pose.z(),
                pose.yaw(), pose.pitch());
        }

    }

}
