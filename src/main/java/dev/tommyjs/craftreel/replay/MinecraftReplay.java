package dev.tommyjs.craftreel.replay;

import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.reel.scene.ReplaySceneImpl;
import dev.tommyjs.reel.scene.player.ScenePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface MinecraftReplay extends Closeable {

    void start();

    void stop();

    ScenePlayer getScenePlayer();

    ReplaySceneImpl getScene();

    Plugin getPlugin();

    void addViewer(Player player);

    void removeViewer(Player player);

    Set<UUID> getViewers();

    Collection<Player> getOnlineViewers();

    SpectatorRegistry getSpectators();

    boolean isViewer(Player player);

    @Override
    void close();

}
