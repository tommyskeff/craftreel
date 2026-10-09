package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.tab.TabHeaderMeta;
import dev.tommyjs.craftreel.protocol.tab.TabHeader;
import dev.tommyjs.reel.recorder.EntityRecorder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


public final class TabHeaderRecorder {

    private final EntityRecorder recorder;

    private TabHeaderRecorder(EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public static @NotNull TabHeaderRecorder attach(@NotNull MinecraftRecording recording, @NotNull Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TAB_HEADER);
        recorder.recordState(CraftReelProtocol.Tracks.TAB_HEADER_META, new TabHeaderMeta(identifier));
        return new TabHeaderRecorder(recorder);
    }

    public static @NotNull TabHeaderRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return recording.getDefault(TabHeaderRecorder.class,
            r -> attach(r, CraftReelProtocol.Defaults.TAB_HEADER));
    }

    public void recordHeader(@NotNull Component header, @NotNull Component footer) {
        recorder.recordState(CraftReelProtocol.Tracks.TAB_HEADER, new TabHeader(header, footer));
    }

}
