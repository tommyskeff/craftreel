package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo.Action;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo.PlayerData;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.tab.TabEntryState;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.ContextGroup;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.ViewerSet;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.reel.scene.SceneResourceKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class TabEntryActor extends AbstractActor implements Viewable {

    private static final SceneResourceKey<UUID, TabEntryActor> OWNER = SceneResourceKey.of("tab_entry_owner");

    private ContextGroup context;
    private UUID profileId;
    private UserProfile profile;
    private TabEntryState state;
    private final ViewerSet viewers = new ViewerSet();
    private boolean registered;
    private TabEntryState lastState;

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.TAB_ENTRY_META, meta -> {
            context = scene.getResourceManager().require(BaseResources.TAB_LIST, meta.contextId());
            profileId = meta.profileId();
            profile = createProfile(profileUuid(meta.contextId(), profileId), meta.name(), meta.skinValue(), meta.skinSignature());
            scene.getResourceManager().publish(BaseResources.TAB_ENTRY, profileId, profile.getUUID());
            scene.getResourceManager().publish(OWNER, profile.getUUID(), this);
        });

        onState(CraftReelProtocol.Tracks.TAB_ENTRY_STATE, true, s -> state = s);

        onFrame(this::render);

        onDestroy(() -> {
            boolean owner = profile != null && scene.getResourceManager().find(OWNER, profile.getUUID()) == this;
            if (registered) {
                if (owner) {
                    for (Player viewer : viewers.online()) {
                        hide(viewer);
                    }
                }
                context.group().remove(this);
            }
            if (owner) {
                scene.getResourceManager().unpublish(OWNER, profile.getUUID());
                scene.getResourceManager().unpublish(BaseResources.TAB_ENTRY, profileId);
            }
        });
    }

    private void render() {
        if (state == null) {
            return;
        }
        if (!registered) {
            lastState = state;
            context.group().add(this);
            registered = true;
            return;
        }
        if (state.equals(lastState)) {
            return;
        }
        for (Player viewer : viewers.online()) {
            if (state.latency() != lastState.latency()) {
                send(viewer, packet(Action.UPDATE_LATENCY));
            }
            if (state.gameMode() != lastState.gameMode()) {
                send(viewer, packet(Action.UPDATE_GAME_MODE));
            }
            if (!Objects.equals(state.displayName(), lastState.displayName())) {
                send(viewer, packet(Action.UPDATE_DISPLAY_NAME));
            }
        }
        lastState = state;
    }

    @Override
    public void addViewer(@NotNull Player player) {
        viewers.add(player);
        show(player);
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        if (viewers.remove(player) && player.isOnline()) {
            hide(player);
        }
    }

    private void show(Player player) {
        if (state == null) {
            return;
        }
        send(player, packet(Action.ADD_PLAYER));
    }

    private void hide(Player player) {
        send(player, new WrapperPlayServerPlayerInfo(Action.REMOVE_PLAYER,
            List.of(new PlayerData(null, profile, GameMode.SURVIVAL, 0))));
    }

    private WrapperPlayServerPlayerInfo packet(Action action) {
        return new WrapperPlayServerPlayerInfo(action, List.of(new PlayerData(
            state.displayName(), profile, gameMode(state.gameMode()), state.latency())));
    }

    private static GameMode gameMode(int id) {
        GameMode mode = GameMode.getById(id);
        return mode == null ? GameMode.SURVIVAL : mode;
    }

    private static UUID profileUuid(Identifier contextId, UUID profileId) {
        return UUID.nameUUIDFromBytes((contextId + "/" + profileId).getBytes(StandardCharsets.UTF_8));
    }

    private static UserProfile createProfile(UUID id, String name, String skinValue, String skinSignature) {
        if (skinValue == null) {
            return new UserProfile(id, name);
        }
        TextureProperty texture = new TextureProperty("textures", skinValue, skinSignature);
        return new UserProfile(id, name, List.of(texture));
    }

    private static void send(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}
