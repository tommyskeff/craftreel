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
import dev.tommyjs.reel.scene.AbstractActor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class TabEntryActor extends AbstractActor implements Viewable {

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
            profile = createProfile(meta.name(), meta.skinValue(), meta.skinSignature());
            scene.getResourceManager().publish(BaseResources.TAB_ENTRY, profileId, profile.getUUID());
        });

        onState(CraftReelProtocol.Tracks.TAB_ENTRY_STATE, true, s -> state = s);

        onFrame(this::render);

        onDestroy(() -> {
            if (registered) {
                for (Player viewer : viewers.online()) {
                    hide(viewer);
                }
                context.group().remove(this);
            }
            if (profileId != null) {
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

    private static UserProfile createProfile(String name, String skinValue, String skinSignature) {
        UUID id = UUID.randomUUID();
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
