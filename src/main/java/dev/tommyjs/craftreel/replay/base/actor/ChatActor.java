package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.replay.reference.ViewerSet;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.protocol.chat.ChatLine;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ChatActor extends AbstractActor implements ViewerContext {

    private Identifier id;
    private final ViewerSet viewers = new ViewerSet();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.TEXT_META, meta -> {
            id = meta.id();
            scene.getResourceManager().publish(BaseResources.CHAT, id, this);
        });

        onEvent(CraftReelProtocol.Tracks.CHAT_MESSAGE, this::broadcast);

        onDestroy(() -> {
            if (id != null) {
                for (SpectatorRegistry registry : scene.getResourceManager().getResources(BaseResources.SPECTATORS)) {
                    registry.purge(this);
                }
                scene.getResourceManager().unpublish(BaseResources.CHAT, id);
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

    private void broadcast(ChatLine line) {
        String message = LegacyComponentSerializer.legacySection().serialize(line.text());
        for (Player viewer : viewers.online()) {
            viewer.sendMessage(message);
        }
    }

}
