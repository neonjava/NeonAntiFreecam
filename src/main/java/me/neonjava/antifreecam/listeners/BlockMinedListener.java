package me.neonjava.antifreecam.listeners;

import me.neonjava.antifreecam.bootstrap.NeonAntiFreecamPlugin;
import me.neonjava.antifreecam.packets.PacketInterceptor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Ensures mined blocks instantly trigger refresh updates to prevent clients seeing void.
 */
public final class BlockMinedListener implements Listener {
    private final PacketInterceptor interceptor;

    public BlockMinedListener(PacketInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        int hideY = NeonAntiFreecamPlugin.getInstance().getConfigManager().getWorldConfig(player.getWorld().getName()).getHideY();
        if (block.getY() < hideY) {
            // Force a refresh chunk surrounding block updates
            interceptor.getStateTracker().resendNearbyChunks(player);
        }
    }
}
