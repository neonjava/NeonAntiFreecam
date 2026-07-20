package me.neonjava.antifreecam.chunk;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player status states and handles instant resending of chunks.
 */
public final class PlayerStateTracker {
    public enum State {
        ABOVE_HIDE_LEVEL,
        BELOW_HIDE_LEVEL
    }

    private final Set<Player> bypassCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<Player, State> playerStates = new ConcurrentHashMap<>();

    public State getState(Player player) {
        return playerStates.getOrDefault(player, State.ABOVE_HIDE_LEVEL);
    }

    public void remove(Player player) {
        playerStates.remove(player);
        bypassCache.remove(player);
    }

    public boolean checkStateChange(Player player, int hideY) {
        if (player.hasPermission("neonantifreecam.bypass")) {
            bypassCache.add(player);
            return false;
        }
        bypassCache.remove(player);

        int eyeY = (int) player.getEyeLocation().getY();
        State currentState = eyeY < hideY ? State.BELOW_HIDE_LEVEL : State.ABOVE_HIDE_LEVEL;
        State oldState = playerStates.put(player, currentState);

        if (oldState == null) {
            return false;
        }

        if (oldState != currentState) {
            resendNearbyChunks(player);
            return true;
        }
        return false;
    }

    public void resendNearbyChunks(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int viewDistance = player.getClientViewDistance();
        if (viewDistance <= 0) {
            viewDistance = player.getServer().getViewDistance();
        }

        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;

        // Resend all chunks in view distance instantly
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                int cx = chunkX + dx;
                int cz = chunkZ + dz;
                if (world.isChunkLoaded(cx, cz)) {
                    // World refreshChunk refreshes chunk view to all tracking players
                    world.refreshChunk(cx, cz);
                }
            }
        }
    }
}
