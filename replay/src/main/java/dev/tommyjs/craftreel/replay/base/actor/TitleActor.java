package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle.TitleAction;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.replay.reference.ViewerSet;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.protocol.title.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TitleActor extends AbstractActor implements ViewerContext {

    private Identifier id;
    private final ViewerSet viewers = new ViewerSet();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.TEXT_META, meta -> {
            id = meta.id();
            scene.getResourceManager().publish(BaseResources.TITLE, id, this);
        });

        onEvent(CraftReelProtocol.Tracks.TITLE, this::broadcast);

        onDestroy(() -> {
            if (id != null) {
                for (SpectatorRegistry registry : scene.getResourceManager().getResources(BaseResources.SPECTATORS)) {
                    registry.purge(this);
                }
                scene.getResourceManager().unpublish(BaseResources.TITLE, id);
            }
            viewers.clear();
        });
    }

    @Override
    public @NotNull Identifier id() {
        return id;
    }

    @Override
    public void addViewer(@NotNull Player player) {
        viewers.add(player);
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        viewers.remove(player);
    }

    private void broadcast(Title title) {
        for (Player viewer : viewers.online()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer,
                new WrapperPlayServerTitle(TitleAction.SET_TIMES_AND_DISPLAY,
                    (Component) null, null, null, title.fadeIn(), title.stay(), title.fadeOut()));
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer,
                new WrapperPlayServerTitle(TitleAction.SET_SUBTITLE,
                    null, title.subtitle(), null, 0, 0, 0));
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer,
                new WrapperPlayServerTitle(TitleAction.SET_TITLE,
                    title.title(), null, null, 0, 0, 0));
        }
    }

}
