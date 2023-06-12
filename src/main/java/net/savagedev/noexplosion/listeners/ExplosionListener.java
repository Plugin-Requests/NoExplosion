package net.savagedev.noexplosion.listeners;

import net.savagedev.noexplosion.NoExplosionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ExplosionListener implements Listener {
    private final Set<Location> clickedRespawnAnchors = new HashSet<>();

    private final NoExplosionPlugin plugin;

    public ExplosionListener(NoExplosionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(EntityExplodeEvent event) {
        final Entity explodingEntity = event.getEntity();

        final Set<EntityType> disabledEntities = this.plugin.getDisabledEntities(explodingEntity.getWorld())
                .orElse(Collections.emptySet());

        if (!disabledEntities.contains(explodingEntity.getType())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void on(BlockExplodeEvent event) {
        final Block explodingBlock = event.getBlock();

        final Set<Material> disabledBlocks = this.plugin.getDisabledBlocks(explodingBlock.getWorld())
                .orElse(Collections.emptySet());

        // For some reason when a respawn anchor explodes, the block passed to the event is AIR.
        final boolean isRespawnAnchor = this.clickedRespawnAnchors.remove(explodingBlock.getLocation());

        if (!disabledBlocks.contains(explodingBlock.getType()) && !isRespawnAnchor) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        final Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null || clickedBlock.getType() != Material.RESPAWN_ANCHOR) {
            return;
        }

        final RespawnAnchor respawnAnchor = (RespawnAnchor) clickedBlock.getBlockData();

        if (respawnAnchor.getCharges() < respawnAnchor.getMaximumCharges()) {
            return;
        }

        this.clickedRespawnAnchors.add(clickedBlock.getLocation());
    }
}
