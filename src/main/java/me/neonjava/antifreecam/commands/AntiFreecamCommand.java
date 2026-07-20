package me.neonjava.antifreecam.commands;

import me.neonjava.antifreecam.bootstrap.NeonAntiFreecamPlugin;
import me.neonjava.antifreecam.metrics.PerformanceMetrics;
import me.neonjava.antifreecam.permissions.PermissionConstants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles administrative commands for `/neonantifreecam`.
 */
public final class AntiFreecamCommand implements CommandExecutor {
    private final NeonAntiFreecamPlugin plugin;
    private final PerformanceMetrics metrics;

    public AntiFreecamCommand(NeonAntiFreecamPlugin plugin, PerformanceMetrics metrics) {
        this.plugin = plugin;
        this.metrics = metrics;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PermissionConstants.ADMIN)) {
            sender.sendMessage(ChatColor.RED + "[NeonAntiFreecam] You do not have permission to execute this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(ChatColor.GREEN + "=== NeonAntiFreecam Status ===");
            sender.sendMessage(ChatColor.YELLOW + "Protected Players: " + ChatColor.WHITE + metrics.getProtectedPlayersCount());
            sender.sendMessage(ChatColor.YELLOW + "Modified Packets: " + ChatColor.WHITE + metrics.getModifiedPacketsCount());
            sender.sendMessage(ChatColor.YELLOW + "Avg Process Time: " + ChatColor.WHITE + String.format("%.4f ms", metrics.getAverageProcessingTimeMs()));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().load();
            sender.sendMessage(ChatColor.GREEN + "[NeonAntiFreecam] Configuration reloaded successfully.");
            return true;
        }

        if (args[0].equalsIgnoreCase("benchmark")) {
            sender.sendMessage(ChatColor.GREEN + "[NeonAntiFreecam] Benchmarking run...");
            sender.sendMessage(ChatColor.YELLOW + "Avg Processing Overhead: " + ChatColor.WHITE + String.format("%.4f ms", metrics.getAverageProcessingTimeMs()));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command argument. Use: /neonantifreecam [status|reload|benchmark]");
        return true;
    }
}
