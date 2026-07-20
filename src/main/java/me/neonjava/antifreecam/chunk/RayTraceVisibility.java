package me.neonjava.antifreecam.chunk;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * High-performance ray tracing visibility checks.
 */
public final class RayTraceVisibility {

    /**
     * Checks if a player has line of sight to an entity.
     */
    public static boolean hasLineOfSight(Location observer, Entity target) {
        if (!observer.getWorld().equals(target.getWorld())) {
            return false;
        }

        Vector start = observer.toVector().add(new Vector(0, observer.getY() - observer.getBlockY(), 0)); // Eye position approximation
        Vector end = target.getLocation().toVector().add(new Vector(0, 0.9, 0)); // Target center approximation
        Vector direction = end.clone().subtract(start).normalize();

        double distance = start.distance(end);
        if (distance > 64) {
            return false;
        }

        int steps = (int) Math.ceil(distance);
        Vector current = start.clone();
        for (int i = 0; i < steps; i++) {
            current.add(direction);
            Block block = observer.getWorld().getBlockAt(current.getBlockX(), current.getBlockY(), current.getBlockZ());
            if (block.getType().isOccluding()) {
                return false;
            }
        }
        return true;
    }
}
