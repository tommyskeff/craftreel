package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record EntityCollect(@NotNull UUID collectorId, int amount) {
}
