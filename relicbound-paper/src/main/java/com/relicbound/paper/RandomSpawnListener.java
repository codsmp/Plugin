package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomSpawnListener implements Listener {
    private final JavaPlugin plugin;

    public RandomSpawnListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!this.plugin.getConfig().getBoolean("season3.random-spawn.enabled", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return;
        }
        World world = player.getWorld();
        Location random = this.pickSafeSpawn(world);
        if (player.isOnline()) {
            player.teleport(random);
            player.sendMessage(ChatColor.DARK_AQUA + "[Witch] " + ChatColor.GRAY + "You awaken in unknown territory.");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!this.plugin.getConfig().getBoolean("season3.random-spawn.enabled", true)) {
            return;
        }
        Player player = event.getPlayer();
        Location random = this.pickSafeSpawn(player.getWorld());
        event.setRespawnLocation(random);
    }

    private Location pickSafeSpawn(World world) {
        int min = Math.max(100, this.plugin.getConfig().getInt("season3.random-spawn.min-radius", 300));
        int max = Math.max(min + 50, this.plugin.getConfig().getInt("season3.random-spawn.max-radius", 3000));

        WorldBorder border = world.getWorldBorder();
        Location borderCenter = border.getCenter();
        double borderRadius = Math.max(128.0D, border.getSize() / 2.0D);
        double usableRadius = Math.max(128.0D, borderRadius - 24.0D);
        int borderBound = (int) Math.floor(usableRadius);
        int effectiveMax = Math.min(max, Math.max(min, borderBound));
        int effectiveMin = Math.min(min, Math.max(64, effectiveMax / 4));

        for (int attempts = 0; attempts < 48; attempts++) {
            int x = this.randomSigned(effectiveMin, effectiveMax);
            int z = this.randomSigned(effectiveMin, effectiveMax);
            if (this.plugin.getConfig().getBoolean("season3.random-spawn.adapt-to-world-border", true)) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    x = borderCenter.getBlockX() + x;
                    z = borderCenter.getBlockZ() + z;
                } else {
                    x = borderCenter.getBlockX() - x;
                    z = borderCenter.getBlockZ() - z;
                }
            }

            if (!this.isInsideBorder(world, x, z)) {
                continue;
            }

            int y = world.getHighestBlockYAt(x, z);
            Location candidate = new Location(world, x + 0.5D, y + 1.0D, z + 0.5D);
            // Avoid spawning in unsafe biomes or too close to other players
            if (!this.isSafe(candidate)) continue;
            boolean nearPlayer = false;
            for (org.bukkit.entity.Player p : world.getPlayers()) {
                if (p.getLocation().distanceSquared(candidate) < (48 * 48)) {
                    nearPlayer = true;
                    break;
                }
            }
            if (nearPlayer) continue;
            // Ensure chunk is loaded to avoid teleporting into unloaded terrain
            if (!candidate.getChunk().isLoaded()) candidate.getChunk().load();
            return candidate;
        }

        Location fallback = world.getSpawnLocation().clone();
        if (!this.isInsideBorder(world, fallback.getBlockX(), fallback.getBlockZ())) {
            fallback = borderCenter.clone();
        }
        fallback.setY(world.getHighestBlockYAt(fallback) + 1.0D);
        return fallback;
    }

    private boolean isInsideBorder(World world, int x, int z) {
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double half = border.getSize() / 2.0D;
        double dx = Math.abs((x + 0.5D) - center.getX());
        double dz = Math.abs((z + 0.5D) - center.getZ());
        return dx <= half && dz <= half;
    }

    private int randomSigned(int min, int max) {
        int absolute = ThreadLocalRandom.current().nextInt(min, max + 1);
        return ThreadLocalRandom.current().nextBoolean() ? absolute : -absolute;
    }

    private boolean isSafe(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block below = feet.getRelative(0, -1, 0);

        if (!below.getType().isSolid()) {
            return false;
        }
        if (!feet.isPassable() || !head.isPassable()) {
            return false;
        }
        return !below.isLiquid();
    }
}
