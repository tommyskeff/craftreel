package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.protocol.chat.ChatLine;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class ChatActor extends AbstractActor implements ViewerContext {

    private Identifier id;
    private final Set<UUID> viewers = new LinkedHashSet<>();

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
    public void attach(@NotNull Player player) {
        viewers.add(player.getUniqueId());
    }

    @Override
    public void detach(@NotNull Player player) {
        viewers.remove(player.getUniqueId());
    }

    private void broadcast(ChatLine line) {
        String text = LegacyComponentSerializer.legacySection().serialize(line.text());
        String message = ChatColor.WHITE + "[" + timestamp(transition.to()) + "] " + ChatColor.RESET + text;
        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                viewer.sendMessage(message);
            }
        }
    }

    private static String timestamp(long frame) {
        long seconds = frame / 20;
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

}
