package dev.tommyjs.craftreel.protocol.world;

public record WorldBorderState(double centerX, double centerZ, double diameter, double targetDiameter, long remainingMillis,
                               int warningBlocks, int warningSeconds) {

    public boolean resizing() {
        return remainingMillis > 0 && diameter != targetDiameter;
    }

}
