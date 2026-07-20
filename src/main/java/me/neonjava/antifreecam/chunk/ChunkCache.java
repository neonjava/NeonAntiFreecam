package me.neonjava.antifreecam.chunk;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches procedural chunk updates per world coordinate to optimize allocation rates.
 */
public final class ChunkCache {
    private final Map<ChunkKey, WrapperPlayServerChunkData> cachedFakeColumns = new ConcurrentHashMap<>();

    public void put(int x, int z, WrapperPlayServerChunkData packet) {
        cachedFakeColumns.put(new ChunkKey(x, z), packet);
    }

    public WrapperPlayServerChunkData get(int x, int z) {
        return cachedFakeColumns.get(new ChunkKey(x, z));
    }

    public void clear() {
        cachedFakeColumns.clear();
    }
}
