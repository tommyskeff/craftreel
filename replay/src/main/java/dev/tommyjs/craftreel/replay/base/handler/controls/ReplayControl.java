package dev.tommyjs.craftreel.replay.base.handler.controls;

public enum ReplayControl {

    SLOWER(2),
    REWIND(3),
    PLAY_PAUSE(4),
    FORWARD(5),
    FASTER(6),
    LIVE(8);

    private final int slot;

    ReplayControl(int slot) {
        this.slot = slot;
    }

    public int slot() {
        return slot;
    }

}
