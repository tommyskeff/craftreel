package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.team.TeamInfo;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.ContextGroup;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.ViewerSet;
import dev.tommyjs.reel.scene.AbstractActor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class TeamActor extends AbstractActor implements Viewable {

    private ContextGroup context;
    private String name;
    private TeamInfo info;
    private Set<String> members = new LinkedHashSet<>();
    private final ViewerSet viewers = new ViewerSet();
    private boolean registered;
    private TeamInfo lastInfo;
    private Set<String> lastMembers = new LinkedHashSet<>();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.TEAM_META, meta -> {
            context = scene.getResourceManager().require(BaseResources.TEAM, meta.contextId());
            name = meta.name();
        });

        onState(CraftReelProtocol.Tracks.TEAM_INFO, true, s -> info = s);

        onState(CraftReelProtocol.Tracks.TEAM_MEMBERS, true, s -> members = s.entries());

        onFrame(this::render);

        onDestroy(() -> {
            if (registered) {
                for (Player viewer : viewers.online()) {
                    hide(viewer);
                }
                context.group().remove(this);
            }
        });
    }

    private void render() {
        if (info == null) {
            return;
        }
        if (!registered) {
            lastInfo = info;
            lastMembers = new LinkedHashSet<>(members);
            context.group().add(this);
            registered = true;
            return;
        }
        if (!info.equals(lastInfo)) {
            for (Player viewer : viewers.online()) {
                send(viewer, new WrapperPlayServerTeams(name, TeamMode.UPDATE, info(info)));
            }
            lastInfo = info;
        }
        Set<String> added = new LinkedHashSet<>(members);
        added.removeAll(lastMembers);
        if (!added.isEmpty()) {
            for (Player viewer : viewers.online()) {
                send(viewer, new WrapperPlayServerTeams(name, TeamMode.ADD_ENTITIES, Optional.empty(), added));
            }
        }
        Set<String> removed = new LinkedHashSet<>(lastMembers);
        removed.removeAll(members);
        if (!removed.isEmpty()) {
            for (Player viewer : viewers.online()) {
                send(viewer, new WrapperPlayServerTeams(name, TeamMode.REMOVE_ENTITIES, Optional.empty(), removed));
            }
        }
        if (!added.isEmpty() || !removed.isEmpty()) {
            lastMembers = new LinkedHashSet<>(members);
        }
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
        if (info == null) {
            return;
        }
        send(player, new WrapperPlayServerTeams(name, TeamMode.CREATE, info(info), members));
    }

    private void hide(Player player) {
        send(player, new WrapperPlayServerTeams(name, TeamMode.REMOVE, Optional.empty()));
    }

    private static ScoreBoardTeamInfo info(TeamInfo team) {
        NamedTextColor color = team.color() != null ? team.color() : NamedTextColor.WHITE;
        return new ScoreBoardTeamInfo(team.displayName(), team.prefix(), team.suffix(),
            NameTagVisibility.valueOf(team.nameTagVisibility().name()),
            CollisionRule.valueOf(team.collisionRule().name()), color, options(team));
    }

    private static OptionData options(TeamInfo team) {
        if (team.friendlyFire() && team.seeFriendlyInvisibles()) {
            return OptionData.ALL;
        } else if (team.friendlyFire()) {
            return OptionData.FRIENDLY_FIRE;
        } else if (team.seeFriendlyInvisibles()) {
            return OptionData.FRIENDLY_CAN_SEE_INVISIBLE;
        } else {
            return OptionData.NONE;
        }
    }

    private static void send(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}
