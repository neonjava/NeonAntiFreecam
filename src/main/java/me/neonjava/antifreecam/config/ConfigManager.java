package me.neonjava.antifreecam.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages configuration loading and per-world configuration resolving.
 */
public final class ConfigManager {
    private final Plugin plugin;
    private boolean globalEnabled = true;
    private boolean bedrockSupport = true;
    private boolean notifyAdmins = true;
    private boolean debug = false;
    
    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private WorldConfig defaultConfig;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        globalEnabled = config.getBoolean("enabled", true);
        bedrockSupport = config.getBoolean("bedrock-support", true);
        notifyAdmins = config.getBoolean("notify-admins", true);
        debug = config.getBoolean("debug", false);

        int defHideY = config.getInt("hide-y", 32);
        int defBubble = config.getInt("bubble-radius", 20);
        boolean defSmart = config.getBoolean("smart-fill", true);
        boolean defEntities = config.getBoolean("hide-entities", true);
        boolean defStructures = config.getBoolean("hide-structures", true);
        boolean defContainers = config.getBoolean("hide-containers", true);
        
        defaultConfig = new WorldConfig(
            globalEnabled, defHideY, defBubble, defSmart, 
            defEntities, defStructures, defContainers,
            config.getStringList("hidden-structures")
        );

        worldConfigs.clear();
        ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        if (worldsSection != null) {
            for (String key : worldsSection.getKeys(false)) {
                ConfigurationSection sec = worldsSection.getConfigurationSection(key);
                if (sec == null) continue;
                WorldConfig wc = new WorldConfig(
                    sec.getBoolean("enabled", defaultConfig.isEnabled()),
                    sec.getInt("hide-y", defaultConfig.getHideY()),
                    sec.getInt("bubble-radius", defaultConfig.getBubbleRadius()),
                    sec.getBoolean("smart-fill", defaultConfig.isSmartFill()),
                    sec.getBoolean("hide-entities", defaultConfig.isHideEntities()),
                    sec.getBoolean("hide-structures", defaultConfig.isHideStructures()),
                    sec.getBoolean("hide-containers", defaultConfig.isHideContainers()),
                    sec.contains("hidden-structures") ? sec.getStringList("hidden-structures") : defaultConfig.getHiddenStructures()
                );
                worldConfigs.put(key.toLowerCase(), wc);
            }
        }
        plugin.getLogger().log(Level.INFO, "[NeonAntiFreecam] Configuration Loaded/Reloaded");
    }

    public WorldConfig getWorldConfig(String worldName) {
        if (!globalEnabled) {
            return new WorldConfig(false, 0, 0, false, false, false, false, Collections.emptyList());
        }
        return worldConfigs.getOrDefault(worldName.toLowerCase(), defaultConfig);
    }

    public boolean isGlobalEnabled() { return globalEnabled; }
    public boolean isBedrockSupport() { return bedrockSupport; }
    public boolean isNotifyAdmins() { return notifyAdmins; }
    public boolean isDebug() { return debug; }
}
