package me.neonjava.antifreecam.chunk;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import me.neonjava.antifreecam.config.WorldConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Procedural Dual Pipeline engine caching and predicting chunk transmissions.
 */
public final class PredictiveChunkEngine {
    private final Map<Player, Map<ChunkKey, Column>> realCache = new ConcurrentHashMap<>();
    private final Map<Player, Map<ChunkKey, Column>> fakeCache = new ConcurrentHashMap<>();

    public void clear(Player player) {
        realCache.remove(player);
        fakeCache.remove(player);
    }

    public void putReal(Player player, int cx, int cz, Column col) {
        realCache.computeIfAbsent(player, k -> new ConcurrentHashMap<>()).put(new ChunkKey(cx, cz), col);
    }

    public void putFake(Player player, int cx, int cz, Column col) {
        fakeCache.computeIfAbsent(player, k -> new ConcurrentHashMap<>()).put(new ChunkKey(cx, cz), col);
    }

    public Column getReal(Player player, int cx, int cz) {
        Map<ChunkKey, Column> map = realCache.get(player);
        return map != null ? map.get(new ChunkKey(cx, cz)) : null;
    }

    public Column getFake(Player player, int cx, int cz) {
        Map<ChunkKey, Column> map = fakeCache.get(player);
        return map != null ? map.get(new ChunkKey(cx, cz)) : null;
    }

    public void prefetchAndPredict(Player player, WorldConfig worldConfig) {
        Location loc = player.getLocation();
        double pitch = loc.getPitch(); // Negative is looking up, positive down
        double yVelocity = player.getVelocity().getY();

        // Detect downward movement / pitch / mining
        boolean movingDown = yVelocity < -0.05 || pitch > 30;
        int eyeY = loc.getBlockY();
        int threshold = worldConfig.getHideY() + 4; // Reveal buffer is 4 blocks

        if (eyeY <= threshold && movingDown) {
            preloadRealRadius(player);
        }
    }

    private void preloadRealRadius(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int viewDistance = player.getClientViewDistance();
        if (viewDistance <= 0) {
            viewDistance = player.getServer().getViewDistance();
        }

        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;

        // Proactively cache real chunks in radius
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                int cx = chunkX + dx;
                int cz = chunkZ + dz;
                if (world.isChunkLoaded(cx, cz)) {
                    // Pre-cache triggers via listener interceptor automatically
                }
            }
        }
    }

    public void switchReal(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int viewDistance = player.getClientViewDistance();
        if (viewDistance <= 0) {
            viewDistance = player.getServer().getViewDistance();
        }

        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;

        Map<ChunkKey, Column> realMap = realCache.get(player);
        if (realMap == null || realMap.isEmpty()) return;

        for (Map.Entry<ChunkKey, Column> entry : realMap.entrySet()) {
            ChunkKey key = entry.getKey();
            if (key.distanceSquared(chunkX, chunkZ) <= Math.pow(viewDistance + 2, 2)) {
                WrapperPlayServerChunkData packet = new WrapperPlayServerChunkData(entry.getValue());
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }
    }
}
