package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarMeta;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarState;
import dev.tommyjs.reel.recorder.EntityRecorder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class SidebarRecorder {

    private static final Map<MinecraftRecording, SidebarRecorder> DEFAULTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final EntityRecorder recorder;

    private SidebarRecorder(@NotNull EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public static @NotNull SidebarRecorder attach(@NotNull MinecraftRecording recording, Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.SIDEBAR);
        recorder.recordState(CraftReelProtocol.Tracks.SIDEBAR_META, new SidebarMeta(identifier));
        return new SidebarRecorder(recorder);
    }

    public static SidebarRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.SIDEBAR));
    }

    public void recordSidebar(@NotNull List<Component> lines) {
        recorder.recordState(CraftReelProtocol.Tracks.SIDEBAR, new SidebarState(lines));
    }

}
