package me.neonjava.antifreecam.chunk;

import java.util.Objects;

/**
 * Clean, lightweight record for holding chunk Coordinates.
 */
public record ChunkKey(int x, int z) {
    public double distanceSquared(int otherX, int otherZ) {
        double dx = this.x - otherX;
        double dz = this.z - otherZ;
        return dx * dx + dz * dz;
    }
}
