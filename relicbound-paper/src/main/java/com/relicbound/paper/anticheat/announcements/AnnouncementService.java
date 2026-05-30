package com.relicbound.paper.anticheat.announcements;

import com.relicbound.paper.anticheat.config.AnticheatConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class AnnouncementService {
    private final AnticheatConfig config;
    private long lastAnnouncementMillis;

    public AnnouncementService(AnticheatConfig config) {
        this.config = config;
    }

    public void announcePunish(String playerName, String reason) {
        if (!this.config.announcements().enabled()) return;
        long now = System.currentTimeMillis();
        if (now - this.lastAnnouncementMillis < (this.config.announcements().cooldownSeconds() * 1000L)) return;
        String title = this.config.announcements().title();
        String msg = ChatColor.GOLD + title + "\n" + ChatColor.WHITE + playerName + " was removed for cheating.\n" + ChatColor.GRAY + "Reason: " + reason;
        if (this.config.announcements().broadcastOnPunish()) {
            Bukkit.broadcastMessage(msg);
        } else {
            Bukkit.getLogger().info(msg);
        }
        this.lastAnnouncementMillis = now;
    }
}
