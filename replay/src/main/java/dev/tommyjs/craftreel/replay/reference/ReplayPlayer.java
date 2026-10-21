package dev.tommyjs.craftreel.replay.reference;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ReplayPlayer {

    @NotNull UUID id();

    int entityId();

    @NotNull String name();

    @Nullable String skinValue();

    @Nullable String skinSignature();

    boolean present();

    @Nullable Location location();

}
