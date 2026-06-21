package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.entity.PlayerMeta;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.dynworld.entity.PlayerEntity;
import dev.tommyjs.dynworld.entity.VirtualEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerActor extends AbstractTrackedActor {

    private boolean tablistOwned;

    public PlayerActor() {
        this(EntityPlaybackConfig.DEFAULT);
    }

    public PlayerActor(EntityPlaybackConfig config) {
        super(config);
    }

    @Override
    protected void registerCreate() {
        onCreate(CraftReelProtocol.Tracks.PLAYER_META, meta -> {
            UUID tabEntryUuid = scene.getResourceManager().find(BaseResources.TAB_ENTRY, meta.profileId());
            this.tablistOwned = tabEntryUuid != null;
            UserProfile profile = tablistOwned
                ? new UserProfile(tabEntryUuid, meta.name())
                : createProfile(meta);
            install(meta.id(), meta.worldId(), tracker -> tracker.spawnPlayer(profile, ORIGIN));
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

}
