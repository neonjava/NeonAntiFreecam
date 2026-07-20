package me.neonjava.antifreecam.entities;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.neonjava.antifreecam.bootstrap.NeonAntiFreecamPlugin;
import me.neonjava.antifreecam.chunk.RayTraceVisibility;
import me.neonjava.antifreecam.config.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Optional;

/**
 * Intercepts outgoing entity spawn/movement packets to filter hidden entities out of range.
 */
public final class EntityPacketFilter extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPlayer() == null) return;
        Player player = (Player) event.getPlayer();

        if (player.hasPermission("neonantifreecam.bypass")) {
            return;
        }

        WorldConfig config = NeonAntiFreecamPlugin.getInstance().getConfigManager().getWorldConfig(player.getWorld().getName());
        if (!config.isEnabled() || !config.isHideEntities()) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(event);
            Optional<UUID> entityUuidOpt = wrapper.getUUID();
            if (entityUuidOpt.isEmpty()) return;

            Entity target = Bukkit.getEntity(entityUuidOpt.get());
            if (target == null) return;

            // Protect underground entities only (below Hide Y)
            if (target.getLocation().getY() < config.getHideY()) {
                if (!RayTraceVisibility.hasLineOfSight(player.getEyeLocation(), target)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
