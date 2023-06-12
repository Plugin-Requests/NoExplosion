package net.savagedev.noexplosion;

import net.savagedev.noexplosion.listeners.ExplosionListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NoExplosionPlugin extends JavaPlugin {
    private final Map<World, Set<EntityType>> disabledEntities = new HashMap<>();
    private final Map<World, Set<Material>> disabledBlocks = new HashMap<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadBlocksAndEntities();
        this.getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
    }

    private void loadBlocksAndEntities() {
        final ConfigurationSection rootSection = this.getConfig().getConfigurationSection("");

        if (rootSection == null) {
            return;
        }

        for (String worldName : rootSection.getKeys(false)) {
            final World world = this.getServer().getWorld(worldName);

            if (world == null) {
                this.getLogger().warning("Unknown world: " + worldName + ". Skipping.");
                continue;
            }

            this.disabledEntities.put(world, new HashSet<>());
            this.disabledBlocks.put(world, new HashSet<>());

            final List<String> blocksAndEntities = rootSection.getStringList(worldName);

            if (blocksAndEntities.isEmpty()) {
                this.getLogger().info("No blocks or entities to load for world " + worldName + " found.");
                continue;
            }

            for (String blockOrEntity : blocksAndEntities) {
                Material material = null;
                try {
                    material = Material.getMaterial(blockOrEntity);
                    this.disabledBlocks.get(world).add(material);
                } catch (IllegalArgumentException ignored) {
                }

                EntityType entityType = null;
                try {
                    entityType = EntityType.valueOf(blockOrEntity);
                    this.disabledEntities.get(world).add(entityType);
                } catch (IllegalArgumentException ignored) {
                }

                if (material == null && entityType == null) {
                    this.getLogger().warning("Invalid block or entity entry: " + blockOrEntity + ". Skipping.");
                }
            }
        }
    }

    public Optional<Set<EntityType>> getDisabledEntities(World world) {
        return Optional.ofNullable(this.disabledEntities.get(world));
    }

    public Optional<Set<Material>> getDisabledBlocks(World world) {
        return Optional.ofNullable(this.disabledBlocks.get(world));
    }
}
