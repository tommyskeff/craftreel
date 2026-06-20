package dev.tommyjs.craftreel.replay.reference;

import dev.tommyjs.craftreel.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface ViewerContext extends Viewable {

    @NotNull Identifier id();

}
