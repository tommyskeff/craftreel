package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.tab.TabEntryMeta;
import dev.tommyjs.craftreel.protocol.tab.TabEntryState;
import dev.tommyjs.craftreel.protocol.tab.TabListMeta;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.reel.recorder.EntityRecorder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class TabListRecorder {

    private static final Map<MinecraftRecording, TabListRecorder> DEFAULTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final MinecraftRecording recording;
    private final Identifier id;
    private final Map<UUID, Entry> entries = new HashMap<>();

    private TabListRecorder(MinecraftRecording recording, Identifier id) {
        this.recording = recording;
        this.id = id;
    }

    public static @NotNull TabListRecorder attach(@NotNull MinecraftRecording recording, @NotNull Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TAB_LIST);
        recorder.recordState(CraftReelProtocol.Tracks.TAB_LIST_META, new TabListMeta(identifier));
        return new TabListRecorder(recording, identifier);
    }

    public static @NotNull TabListRecorder attachDefault(@NotNull MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.TAB_LIST));
    }

    public void recordTabEntry(@NotNull UUID profileId, @NotNull String name, @Nullable String skinValue,
                               @Nullable String skinSignature, @Nullable Component displayName, int latency, int gameMode) {
        TabEntryState state = new TabEntryState(latency, gameMode, displayName);
        Entry entry = entries.get(profileId);
        if (entry == null) {
            EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TAB_ENTRY);
            recorder.recordState(CraftReelProtocol.Tracks.TAB_ENTRY_META,
                new TabEntryMeta(id, profileId, name, skinValue, skinSignature));
            recorder.recordState(CraftReelProtocol.Tracks.TAB_ENTRY_STATE, state);
            entries.put(profileId, new Entry(recorder, state));
        } else if (!state.equals(entry.lastState)) {
            entry.recorder.recordState(CraftReelProtocol.Tracks.TAB_ENTRY_STATE, state);
            entry.lastState = state;
        }
    }

    public void remove(@NotNull UUID profileId) {
        Entry entry = entries.remove(profileId);
        if (entry != null) {
            entry.recorder.close();
        }
    }

    private static final class Entry {

        private final EntityRecorder recorder;
        private TabEntryState lastState;

        private Entry(EntityRecorder recorder, TabEntryState lastState) {
            this.recorder = recorder;
            this.lastState = lastState;
        }

    }

}
