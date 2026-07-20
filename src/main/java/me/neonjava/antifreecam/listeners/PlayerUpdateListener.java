package me.neonjava.antifreecam.listeners;

import me.neonjava.antifreecam.bootstrap.NeonAntiFreecamPlugin;
import me.neonjava.antifreecam.packets.PacketInterceptor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listens to player updates to invalidate caches and trigger immediate resends when moving
 * across threshold limits or teleporting.
 */
public final class PlayerUpdateListener implements Listener {
    private final PacketInterceptor interceptor;

    public PlayerUpdateListener(PacketInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("neonantifreecam.bypass")) return;

        int hideY = NeonAntiFreecamPlugin.getInstance().getConfigManager().getWorldConfig(player.getWorld().getName()).getHideY();
        interceptor.getStateTracker().checkStateChange(player, hideY);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        interceptor.getStateTracker().remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        interceptor.getStateTracker().remove(player);
        interceptor.getStateTracker().resendNearbyChunks(player);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        interceptor.getStateTracker().remove(player);
        interceptor.getStateTracker().resendNearbyChunks(player);
    }
}
