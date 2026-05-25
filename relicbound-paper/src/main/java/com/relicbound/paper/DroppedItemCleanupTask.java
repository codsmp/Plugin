package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class DroppedItemCleanupTask {
    private final JavaPlugin plugin;

    public DroppedItemCleanupTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof Item droppedItem) {
                            droppedItem.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 20L * 60L * 5L, 20L * 60L * 5L);
    }
}