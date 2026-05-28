package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerVisibilityEnforcer implements Listener {
    private final JavaPlugin plugin;
    private final PlayerTeamStore teamStore;
    private final PlayerTrustStore trustStore;

    public PlayerVisibilityEnforcer(JavaPlugin plugin, PlayerTeamStore teamStore, PlayerTrustStore trustStore) {
        this.plugin = plugin;
        this.teamStore = teamStore;
        this.trustStore = trustStore;
        // Run initial enforcement on next tick
        Bukkit.getScheduler().runTask(this.plugin, this::enforceAll);
        // Also schedule a periodic enforcement every 15 seconds to recover from external changes
        Bukkit.getScheduler().runTaskTimer(this.plugin, this::enforceAll, 20L * 15L, 20L * 15L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(this.plugin, () -> this.enforceFor(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player left = event.getPlayer();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(left.getUniqueId())) continue;
            try {
                other.hidePlayer(this.plugin, left);
                left.hidePlayer(this.plugin, other);
            } catch (Throwable ignored) {}
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.enforceFor(p));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.enforceFor(p));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        Bukkit.getScheduler().runTask(this.plugin, () -> this.enforceFor(p));
    }

    private void enforceAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            enforceFor(viewer);
        }
    }

    private void enforceFor(Player viewer) {
        if (!viewer.isOnline()) return;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.isOnline()) continue;
            if (viewer.getUniqueId().equals(target.getUniqueId())) {
                viewer.showPlayer(this.plugin, target);
                continue;
            }
            try {
                boolean allowed = false;
                if (this.trustStore != null && this.trustStore.isTrustedEitherWay(viewer.getUniqueId().toString(), target.getUniqueId().toString())) {
                    allowed = true;
                }
                if (!allowed && this.teamStore != null && this.teamStore.isAlliedOrSame(viewer.getUniqueId().toString(), target.getUniqueId().toString())) {
                    allowed = true;
                }
                if (allowed) {
                    viewer.showPlayer(this.plugin, target);
                } else {
                    viewer.hidePlayer(this.plugin, target);
                }
            } catch (Throwable ignored) {}
        }
    }
}
