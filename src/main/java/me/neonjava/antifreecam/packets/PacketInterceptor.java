package me.neonjava.antifreecam.packets;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import me.neonjava.antifreecam.bootstrap.NeonAntiFreecamPlugin;
import me.neonjava.antifreecam.chunk.FakeChunkEngine;
import me.neonjava.antifreecam.chunk.PlayerStateTracker;
import me.neonjava.antifreecam.config.WorldConfig;
import org.bukkit.entity.Player;

/**
 * Intercepts chunk data, block updates, and redirects them to the FakeChunkEngine
 * for processing when below Hide Y.
 */
public final class PacketInterceptor extends SimplePacketListenerAbstract {
    private final FakeChunkEngine fakeChunkEngine = new FakeChunkEngine();
    private final PlayerStateTracker stateTracker = new PlayerStateTracker();

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPlayer() == null) return;
        Player player = (Player) event.getPlayer();

        if (player.hasPermission("neonantifreecam.bypass")) {
            return;
        }

        WorldConfig config = NeonAntiFreecamPlugin.getInstance().getConfigManager().getWorldConfig(player.getWorld().getName());
        if (!config.isEnabled()) return;

        // Check player Y coordinate transition state
        stateTracker.checkStateChange(player, config.getHideY());

        if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
            // If player is below Hide Y, we serve them REAL chunks without replacement
            if (stateTracker.getState(player) == PlayerStateTracker.State.BELOW_HIDE_LEVEL) {
                return;
            }

            WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
            Column col = wrapper.getColumn();
            if (col == null) return;

            int renderDistance = player.getClientViewDistance();
            if (renderDistance <= 0) {
                renderDistance = player.getServer().getViewDistance();
            }

            int pX = player.getLocation().getBlockX() >> 4;
            int pZ = player.getLocation().getBlockZ() >> 4;

            Column processed = fakeChunkEngine.processColumn(player.getWorld(), col, config, pX, pZ, renderDistance);
            wrapper.setColumn(processed);
            event.markForReEncode(true);
        }
    }

    public PlayerStateTracker getStateTracker() {
        return stateTracker;
    }
}
