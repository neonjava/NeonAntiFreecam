package me.neonjava.antifreecam.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Clean Folia-safe scheduler abstraction that checks Folia class presence
 * and delegates to RegionScheduler/GlobalRegionScheduler or standard BukkitScheduler.
 */
public final class FoliaScheduler {
    private static final boolean IS_FOLIA;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            folia = true;
        } catch (ClassNotFoundException ignored) {}
        IS_FOLIA = folia;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runGlobal(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runGlobalAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void runGlobalTimerAsync(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        long delayMillis = delayTicks * 50;
        long periodMillis = periodTicks * 50;
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delayMillis, periodMillis, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        }
    }

    public static void runRegional(Plugin plugin, Location loc, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().execute(plugin, loc, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runEntity(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            entity.getScheduler().execute(plugin, task, null, 1L);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
}
