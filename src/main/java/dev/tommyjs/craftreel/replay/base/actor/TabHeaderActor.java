package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.reel.scene.AbstractActor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class TabHeaderActor extends AbstractActor implements ViewerContext {

    private Identifier id;
    private boolean published;
    private Component header = Component.empty();
    private Component footer = Component.empty();
    private final Set<UUID> viewers = new LinkedHashSet<>();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.TAB_HEADER_META, meta -> id = meta.id());

        onState(CraftReelProtocol.Tracks.TAB_HEADER, true, header -> {
            this.header = header.header();
            this.footer = header.footer();
            if (!published && id != null) {
                scene.getResourceManager().publish(BaseResources.TAB_HEADER, id, this);
                published = true;
            }
        });

        onFrame(this::refresh);

        onDestroy(() -> {
            if (id != null && published) {
                for (SpectatorRegistry registry : scene.getResourceManager().getResources(BaseResources.SPECTATORS)) {
                    registry.purge(this);
                }
                scene.getResourceManager().unpublish(BaseResources.TAB_HEADER, id);
            }
            for (UUID id : viewers) {
                Player viewer = Bukkit.getPlayer(id);
                if (viewer != null && viewer.isOnline()) {
                    clear(viewer);
                }
            }
            viewers.clear();
        });
    }

    @Override
    public @NotNull Identifier id() {
        return id;
    }

    @Override
    public void attach(@NotNull Player player) {
        viewers.add(player.getUniqueId());
        paint(player);
    }

    @Override
    public void detach(@NotNull Player player) {
        if (viewers.remove(player.getUniqueId()) && player.isOnline()) {
            clear(player);
        }
    }

    private void refresh() {
        viewers.removeIf(id -> {
            Player viewer = Bukkit.getPlayer(id);
            return viewer == null || !viewer.isOnline();
        });

        for (UUID id : viewers) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                paint(viewer);
            }
        }
    }

    private void paint(Player player) {
        send(player, header, footer);
    }

    private static void clear(Player player) {
        send(player, Component.empty(), Component.empty());
    }

    private static void send(Player player, Component header, Component footer) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,
            new WrapperPlayServerPlayerListHeaderAndFooter(header, footer));
    }

}
