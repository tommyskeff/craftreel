package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.protocol.title.Title;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class TitleActor extends AbstractActor implements ViewerContext {

    private Identifier id;
    private final Set<UUID> viewers = new LinkedHashSet<>();

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
    public void attach(@NotNull Player player) {
        viewers.add(player.getUniqueId());
    }

    @Override
    public void detach(@NotNull Player player) {
        viewers.remove(player.getUniqueId());
    }

    private void broadcast(Title title) {
        String titleText = LegacyComponentSerializer.legacySection().serialize(title.title());
        String subtitleText = LegacyComponentSerializer.legacySection().serialize(title.subtitle());

        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                viewer.sendTitle(titleText, subtitleText);
            }
        }
    }

}
