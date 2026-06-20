package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.tab.TabMeta;
import dev.tommyjs.craftreel.protocol.tab.TabHeader;
import dev.tommyjs.reel.recorder.EntityRecorder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class TabHeaderRecorder {

    private static final Map<MinecraftRecording, TabHeaderRecorder> DEFAULTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final EntityRecorder recorder;

    private TabHeaderRecorder(EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public static @NotNull TabHeaderRecorder attach(@NotNull MinecraftRecording recording, @NotNull Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TAB_HEADER);
        recorder.recordState(CraftReelProtocol.Tracks.TAB_HEADER_META, new TabMeta(identifier));
        return new TabHeaderRecorder(recorder);
    }

    public static @NotNull TabHeaderRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.TAB_HEADER));
    }

    public void recordHeader(@NotNull Component header, @NotNull Component footer) {
        recorder.recordState(CraftReelProtocol.Tracks.TAB_HEADER, new TabHeader(header, footer));
    }

}
