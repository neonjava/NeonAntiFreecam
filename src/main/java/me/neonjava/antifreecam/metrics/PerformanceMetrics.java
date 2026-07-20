package me.neonjava.antifreecam.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks performance, processing speeds, and packet statistics.
 */
public final class PerformanceMetrics {
    private final AtomicLong protectedPlayers = new AtomicLong(0);
    private final AtomicLong modifiedPackets = new AtomicLong(0);
    private final AtomicLong hiddenEntities = new AtomicLong(0);
    private final AtomicLong totalProcessingTimeNs = new AtomicLong(0);
    private final AtomicLong processingCount = new AtomicLong(0);

    public void incrementProtectedPlayers() { protectedPlayers.incrementAndGet(); }
    public void decrementProtectedPlayers() { protectedPlayers.decrementAndGet(); }
    public void incrementModifiedPackets() { modifiedPackets.incrementAndGet(); }
    public void incrementHiddenEntities() { hiddenEntities.incrementAndGet(); }
    public void recordProcessingTime(long ns) {
        totalProcessingTimeNs.addAndGet(ns);
        processingCount.incrementAndGet();
    }

    public long getProtectedPlayersCount() { return protectedPlayers.get(); }
    public long getModifiedPacketsCount() { return modifiedPackets.get(); }
    public long getHiddenEntitiesCount() { return hiddenEntities.get(); }
    
    public double getAverageProcessingTimeMs() {
        long count = processingCount.get();
        if (count == 0) return 0.0;
        return (totalProcessingTimeNs.get() / (double) count) / 1_000_000.0;
    }

    public void reset() {
        modifiedPackets.set(0);
        hiddenEntities.set(0);
        totalProcessingTimeNs.set(0);
        processingCount.set(0);
    }
}
