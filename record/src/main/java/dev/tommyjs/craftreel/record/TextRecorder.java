package dev.tommyjs.craftreel.record;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.chat.ChatLine;
import dev.tommyjs.craftreel.protocol.text.TextContext;
import dev.tommyjs.craftreel.protocol.title.Title;
import dev.tommyjs.reel.recorder.EntityRecorder;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class TextRecorder {

    private static final Map<MinecraftRecording, TextRecorder> DEFAULTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final EntityRecorder recorder;

    private TextRecorder(EntityRecorder recorder) {
        this.recorder = recorder;
    }

    public static TextRecorder attach(MinecraftRecording recording, Identifier identifier) {
        EntityRecorder recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.TEXT);
        recorder.recordState(CraftReelProtocol.Tracks.TEXT_META, new TextContext(identifier));
        return new TextRecorder(recorder);
    }

    public static TextRecorder attachDefault(MinecraftRecording recording) {
        return DEFAULTS.computeIfAbsent(recording,
            r -> attach(r, CraftReelProtocol.Defaults.TEXT));
    }

    public void recordChat(Component text) {
        recorder.recordEvent(CraftReelProtocol.Tracks.CHAT_MESSAGE, new ChatLine(text));
    }

    @Deprecated
    public void chat(Component text) {
        recordChat(text);
    }

    public void recordTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        recorder.recordEvent(CraftReelProtocol.Tracks.TITLE, new Title(title, subtitle, fadeIn, stay, fadeOut));
    }

    @Deprecated
    public void title(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        recordTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

}
