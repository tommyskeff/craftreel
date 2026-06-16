package dev.tommyjs.craftreel.replay.base.actor;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.reel.scene.AbstractActor;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SidebarActor extends AbstractActor implements ViewerContext {

    private static final String TITLE = ChatColor.GOLD.toString() + ChatColor.BOLD + "REPLAYS";
    private static final String FOOTER = ChatColor.YELLOW + "tommyjs.dev";

    private Identifier id;
    private boolean published;
    private List<Component> content = new ArrayList<>();
    private final Map<UUID, FastBoard> boards = new LinkedHashMap<>();

    @Override
    protected void configure() {
        onCreate(CraftReelProtocol.Tracks.SIDEBAR_META, meta -> id = meta.id());

        onState(CraftReelProtocol.Tracks.SIDEBAR, true, state -> {
            content = new ArrayList<>(state.lines());
            if (!published && id != null) {
                scene.getResourceManager().publish(BaseResources.SIDEBAR, id, this);
                published = true;
            }
        });

        onFrame(this::refreshBoards);

        onDestroy(() -> {
            if (id != null && published) {
                for (SpectatorRegistry registry : scene.getResourceManager().getResources(BaseResources.SPECTATORS)) {
                    registry.purge(this);
                }
                scene.getResourceManager().unpublish(BaseResources.SIDEBAR, id);
            }
            for (FastBoard board : boards.values()) {
                safeDelete(board);
            }
            boards.clear();
        });
    }

    @Override
    public @NotNull Identifier id() {
        return id;
    }

    @Override
    public void attach(@NotNull Player player) {
        FastBoard board = boards.computeIfAbsent(player.getUniqueId(), id -> new FastBoard(player));
        paint(board);
    }

    @Override
    public void detach(@NotNull Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            safeDelete(board);
        }
    }

    private void refreshBoards() {
        Iterator<Map.Entry<UUID, FastBoard>> it = boards.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, FastBoard> entry = it.next();
            FastBoard board = entry.getValue();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline() || board.isDeleted()) {
                safeDelete(board);
                it.remove();
                continue;
            }
            paint(board);
        }
    }

    private void paint(FastBoard board) {
        if (board.isDeleted()) {
            return;
        }
        board.updateTitle(TITLE);
        board.updateLines(layout());
    }

    private List<String> layout() {
        List<String> lines = new ArrayList<>(content.size() + 3);
        lines.add("");
        for (Component line : content) {
            lines.add(LegacyComponentSerializer.legacySection().serialize(line));
        }
        lines.add("");
        lines.add(FOOTER);
        return lines;
    }

    private static void safeDelete(FastBoard board) {
        try {
            if (!board.isDeleted()) {
                board.delete();
            }
        } catch (RuntimeException ignored) {

        }
    }

}
