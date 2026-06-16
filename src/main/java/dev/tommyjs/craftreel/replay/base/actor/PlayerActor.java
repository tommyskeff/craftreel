package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.entity.PlayerMeta;

import java.util.List;
import java.util.UUID;

public final class PlayerActor extends AbstractTrackedActor {

    @Override
    protected void registerCreate() {
        onCreate(CraftReelProtocol.Tracks.PLAYER_META, meta -> {
            UserProfile profile = createProfile(meta);
            install(meta.id(), meta.worldId(), tracker -> tracker.spawnPlayer(profile, ORIGIN));
        });
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
