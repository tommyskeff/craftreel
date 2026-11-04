package dev.tommyjs.craftreel.protocol.entity;

import org.jetbrains.annotations.Nullable;

public enum PaintingArt {

    KEBAB("Kebab", 16, 16),
    AZTEC("Aztec", 16, 16),
    ALBAN("Alban", 16, 16),
    AZTEC2("Aztec2", 16, 16),
    BOMB("Bomb", 16, 16),
    PLANT("Plant", 16, 16),
    WASTELAND("Wasteland", 16, 16),
    POOL("Pool", 32, 16),
    COURBET("Courbet", 32, 16),
    SEA("Sea", 32, 16),
    SUNSET("Sunset", 32, 16),
    CREEBET("Creebet", 32, 16),
    WANDERER("Wanderer", 16, 32),
    GRAHAM("Graham", 16, 32),
    MATCH("Match", 32, 32),
    BUST("Bust", 32, 32),
    STAGE("Stage", 32, 32),
    VOID("Void", 32, 32),
    SKULL_AND_ROSES("SkullAndRoses", 32, 32),
    WITHER("Wither", 32, 32),
    FIGHTERS("Fighters", 64, 32),
    POINTER("Pointer", 64, 64),
    PIG_SCENE("Pigscene", 64, 64),
    BURNING_SKULL("BurningSkull", 64, 64),
    SKELETON("Skeleton", 64, 48),
    DONKEY_KONG("DonkeyKong", 64, 48);

    private final String title;
    private final int widthPixels;
    private final int heightPixels;

    PaintingArt(String title, int widthPixels, int heightPixels) {
        this.title = title;
        this.widthPixels = widthPixels;
        this.heightPixels = heightPixels;
    }

    public static @Nullable PaintingArt byId(int id) {
        PaintingArt[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }

    public int id() {
        return ordinal();
    }

    public String title() {
        return title;
    }

    public int widthPixels() {
        return widthPixels;
    }

    public int heightPixels() {
        return heightPixels;
    }

}
