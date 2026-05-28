package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public final class BlissLifecycleListener implements Listener {
    private final JavaPlugin plugin;
    private final PaperSpellEngine spellEngine;

    public BlissLifecycleListener(JavaPlugin plugin, PaperSpellEngine spellEngine) {
        this.plugin = plugin;
        this.spellEngine = spellEngine;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location loc = this.spellEngine.popBlissReturn(player.getUniqueId());
        if (loc != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (!player.isOnline()) return;
                player.teleport(loc);
                player.sendMessage(org.bukkit.ChatColor.GRAY + "You have been returned from the Bliss.");
            });
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location loc = this.spellEngine.popBlissReturn(player.getUniqueId());
        if (loc != null) {
            event.setRespawnLocation(loc);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // If a player dies while in Bliss, ensure they will respawn at return location.
        if (this.spellEngine.hasBlissReturn(player.getUniqueId())) {
            Location loc = this.spellEngine.popBlissReturn(player.getUniqueId());
            if (loc != null) {
                player.setBedSpawnLocation(loc, true);
            }
        }
    }
}
