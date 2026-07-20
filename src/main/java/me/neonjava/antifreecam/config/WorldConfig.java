package me.neonjava.antifreecam.config;

import java.util.Collections;
import java.util.List;

/**
 * Represents the configuration options for a single world.
 */
public final class WorldConfig {
    private final boolean enabled;
    private final int hideY;
    private final int bubbleRadius;
    private final boolean smartFill;
    private final boolean hideEntities;
    private final boolean hideStructures;
    private final boolean hideContainers;
    private final List<String> hiddenStructures;

    public WorldConfig(boolean enabled, int hideY, int bubbleRadius, boolean smartFill,
                       boolean hideEntities, boolean hideStructures, boolean hideContainers,
                       List<String> hiddenStructures) {
        this.enabled = enabled;
        this.hideY = hideY;
        this.bubbleRadius = bubbleRadius;
        this.smartFill = smartFill;
        this.hideEntities = hideEntities;
        this.hideStructures = hideStructures;
        this.hideContainers = hideContainers;
        this.hiddenStructures = hiddenStructures != null ? List.copyOf(hiddenStructures) : Collections.emptyList();
    }

    public boolean isEnabled() { return enabled; }
    public int getHideY() { return hideY; }
    public int getBubbleRadius() { return bubbleRadius; }
    public boolean isSmartFill() { return smartFill; }
    public boolean isHideEntities() { return hideEntities; }
    public boolean isHideStructures() { return hideStructures; }
    public boolean isHideContainers() { return hideContainers; }
    public List<String> getHiddenStructures() { return hiddenStructures; }
}
