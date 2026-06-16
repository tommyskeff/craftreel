package dev.tommyjs.craftreel.replay.base;

import dev.tommyjs.craftreel.replay.handler.SpectatorRegistry;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.replay.reference.ContextSlot;
import dev.tommyjs.craftreel.replay.reference.Viewable;
import dev.tommyjs.craftreel.replay.reference.ViewerContext;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.reel.scene.SceneResourceKey;

import java.util.UUID;
import java.util.function.IntSupplier;

public final class BaseResources {

    public static final SceneResourceKey<Identifier, WorldContext> WORLD = SceneResourceKey.of("world");
    public static final SceneResourceKey<Identifier, ViewerContext> SIDEBAR = SceneResourceKey.of("sidebar");
    public static final SceneResourceKey<Identifier, ViewerContext> TAB_HEADER = SceneResourceKey.of("tab_header");
    public static final SceneResourceKey<UUID, IntSupplier> ENTITY_ID = SceneResourceKey.of("entity_id");
    public static final SceneResourceKey<Identifier, ViewerContext> CHAT = SceneResourceKey.of("chat");
    public static final SceneResourceKey<Identifier, ViewerContext> TITLE = SceneResourceKey.of("title");
    public static final SceneResourceKey<Identifier, Viewable> EFFECTS = SceneResourceKey.of("effects");
    public static final SceneResourceKey<String, SpectatorRegistry> SPECTATORS = SceneResourceKey.of("spectators");

    private BaseResources() {
    }

    public static final class Slots {

        public static final ContextSlot<WorldContext> WORLD = ContextSlot.of("world");
        public static final ContextSlot<ViewerContext> SIDEBAR = ContextSlot.of("sidebar");
        public static final ContextSlot<ViewerContext> TAB_HEADER = ContextSlot.of("tab_header");

        private Slots() {
        }

    }

}
