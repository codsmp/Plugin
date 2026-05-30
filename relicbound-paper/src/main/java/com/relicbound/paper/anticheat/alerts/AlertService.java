package com.relicbound.paper.anticheat.alerts;

import com.relicbound.paper.anticheat.config.AnticheatConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public final class AlertService {
    private final AnticheatConfig config;
    private final Map<String, Long> lastAlert = new HashMap<>();

    public AlertService(AnticheatConfig config) {
        this.config = config;
    }

    public void alertStaff(String message, String verbose) {
        if (!this.config.alerts().enabled()) return;
        long now = System.currentTimeMillis();
        for (var p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(this.config.alerts().permission())) continue;
            long last = this.lastAlert.getOrDefault(p.getName(), 0L);
            if (now - last < (this.config.alerts().cooldownSeconds() * 1000L)) continue;
            p.sendMessage(ChatColor.RED + "[Witch AC] " + ChatColor.WHITE + message);
            if (this.config.alerts().verbose()) {
                p.sendMessage(ChatColor.DARK_GRAY + verbose);
            }
            this.lastAlert.put(p.getName(), now);
        }
    }
}
