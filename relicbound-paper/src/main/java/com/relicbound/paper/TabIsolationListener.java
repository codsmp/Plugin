package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class TabIsolationListener implements Listener {
    private final JavaPlugin plugin;

    public TabIsolationListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.applyIsolationFor(joined), 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player left = event.getPlayer();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(left.getUniqueId())) {
                continue;
            }
            online.hidePlayer(this.plugin, left);
            left.hidePlayer(this.plugin, online);
        }
    }

    private void applyIsolationFor(Player target) {
        if (!target.isOnline()) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            target.hidePlayer(this.plugin, online);
            online.hidePlayer(this.plugin, target);
        }
    }
}
