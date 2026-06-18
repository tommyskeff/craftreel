package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.sidebar.SidebarDelta;
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
    private final java.util.List<net.kyori.adventure.text.Component> last = new java.util.ArrayList<>();

    private SidebarRecorder(@NotNull EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public static @NotNull SidebarRecorder attach(@NotNull MinecraftRecording recording, Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.SIDEBAR);
        recorder.recordState(CraftReelProtocol.Tracks.SIDEBAR_META, new SidebarMeta(identifier));
        recorder.recordState(CraftReelProtocol.Tracks.SIDEBAR, new SidebarState(java.util.List.of()));
        return new SidebarRecorder(recorder);
    }

    public static SidebarRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.SIDEBAR));
    }

    public void recordSidebar(@NotNull List<Component> lines) {
        for (int i = 0; i < Math.min(last.size(), lines.size()); i++) {
            if (!last.get(i).equals(lines.get(i))) {
                recorder.recordDelta(CraftReelProtocol.Tracks.SIDEBAR, new SidebarDelta.SetLine(i, last.get(i), lines.get(i)));
            }
        }
        for (int i = last.size(); i < lines.size(); i++) {
            recorder.recordDelta(CraftReelProtocol.Tracks.SIDEBAR, new SidebarDelta.AddLine(i, lines.get(i)));
        }
        for (int i = last.size() - 1; i >= lines.size(); i--) {
            recorder.recordDelta(CraftReelProtocol.Tracks.SIDEBAR, new SidebarDelta.RemoveLine(i, last.get(i)));
        }
        last.clear();
        last.addAll(lines);
    }

}
