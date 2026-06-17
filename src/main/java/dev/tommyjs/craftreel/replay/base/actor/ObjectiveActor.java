package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.ObjectiveMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.RenderType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore.Action;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.scoreboard.ObjectiveInfo;
import dev.tommyjs.craftreel.protocol.scoreboard.ScoreboardRenderType;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.ContextGroup;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.ViewerSet;
import dev.tommyjs.reel.scene.AbstractActor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ObjectiveActor extends AbstractActor implements Viewable {

    private ContextGroup context;
    private String name;
    private ObjectiveInfo info;
    private Map<String, Integer> scores = new LinkedHashMap<>();
    private final ViewerSet viewers = new ViewerSet();
    private boolean registered;
    private ObjectiveInfo lastInfo;
    private Map<String, Integer> lastScores = new LinkedHashMap<>();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.OBJECTIVE_META, meta -> {
            context = scene.getResourceManager().require(BaseResources.SCOREBOARD, meta.contextId());
            name = meta.name();
        });

        onState(CraftReelProtocol.Tracks.OBJECTIVE_INFO, true, s -> info = s);

        onState(CraftReelProtocol.Tracks.OBJECTIVE_SCORES, true, s -> scores = s.scores());

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
            lastScores = new LinkedHashMap<>(scores);
            context.group().add(this);
            registered = true;
            return;
        }
        if (!info.equals(lastInfo)) {
            for (Player viewer : viewers.online()) {
                send(viewer, new WrapperPlayServerScoreboardObjective(name, ObjectiveMode.UPDATE,
                    info.displayName(), renderType(info.renderType())));
                if (info.slot() != null && !info.slot().equals(lastInfo.slot())) {
                    send(viewer, new WrapperPlayServerDisplayScoreboard(info.slot(), name));
                }
            }
            lastInfo = info;
        }
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            Integer old = lastScores.get(entry.getKey());
            if (old == null || !old.equals(entry.getValue())) {
                for (Player viewer : viewers.online()) {
                    send(viewer, new WrapperPlayServerUpdateScore(entry.getKey(), Action.CREATE_OR_UPDATE_ITEM,
                        name, Optional.of(entry.getValue())));
                }
            }
        }
        for (String entry : lastScores.keySet()) {
            if (!scores.containsKey(entry)) {
                for (Player viewer : viewers.online()) {
                    send(viewer, new WrapperPlayServerUpdateScore(entry, Action.REMOVE_ITEM, name, Optional.empty()));
                }
            }
        }
        lastScores = new LinkedHashMap<>(scores);
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
        send(player, new WrapperPlayServerScoreboardObjective(name, ObjectiveMode.CREATE,
            info.displayName(), renderType(info.renderType())));
        if (info.slot() != null) {
            send(player, new WrapperPlayServerDisplayScoreboard(info.slot(), name));
        }
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            send(player, new WrapperPlayServerUpdateScore(entry.getKey(), Action.CREATE_OR_UPDATE_ITEM,
                name, Optional.of(entry.getValue())));
        }
    }

    private void hide(Player player) {
        send(player, new WrapperPlayServerScoreboardObjective(name, ObjectiveMode.REMOVE,
            info != null ? info.displayName() : Component.empty(),
            renderType(info != null ? info.renderType() : ScoreboardRenderType.INTEGER)));
    }

    private static RenderType renderType(ScoreboardRenderType type) {
        return type == ScoreboardRenderType.HEARTS ? RenderType.HEARTS : RenderType.INTEGER;
    }

    private static void send(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}
