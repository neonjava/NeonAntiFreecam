package me.neonjava.antifreecam.bootstrap;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.neonjava.antifreecam.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin bootstrap loader. Initializes config, packet listeners, and commands.
 */
public final class NeonAntiFreecamPlugin extends JavaPlugin {
    private static NeonAntiFreecamPlugin instance;
    private ConfigManager configManager;

    @Override
    public void onLoad() {
        instance = this;
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "[NeonAntiFreecam] Plugin Loading...");

        // Load configuration
        configManager = new ConfigManager(this);
        configManager.load();

        // Initialize PacketEvents
        PacketEvents.getAPI().init();

        getLogger().log(Level.INFO, "[NeonAntiFreecam] Plugin Loaded");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getLogger().log(Level.INFO, "[NeonAntiFreecam] Plugin Disabled");
    }

    public static NeonAntiFreecamPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
