package com.relicbound.paper;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;

public final class OneDayBanListener implements Listener {
    private static final long ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L;

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.isBlank()) {
            return;
        }

        if (this.handleCommand(event.getPlayer().getName(), message.substring(1), event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        if (command == null || command.isBlank()) {
            return;
        }

        if (this.handleCommand(event.getSender().getName(), command, event.getSender().getName())) {
            event.setCancelled(true);
        }
    }

    private boolean handleCommand(String sourceName, String commandLine, String sourceLabel) {
        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length == 0) {
            return false;
        }

        String base = parts[0].toLowerCase();
        if (!base.equals("ban") && !base.equals("ban-ip") && !base.equals("banip")) {
            return false;
        }

        if (parts.length < 2) {
            return true;
        }

        long expiryMillis = System.currentTimeMillis() + ONE_DAY_MILLIS;
        Date expiry = new Date(expiryMillis);
        String target = parts[1];
        String reason = parts.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length)) : "Banned for 1 day.";

        if (base.equals("ban")) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason, expiry, sourceLabel);
            Player online = Bukkit.getPlayerExact(target);
            if (online != null) {
                online.kickPlayer(reason + "\nBan expires in 1 day.");
            }
            return true;
        }

        String ipAddress = this.resolveBanIpTarget(target);
        if (ipAddress == null || ipAddress.isBlank()) {
            return true;
        }

        Bukkit.getBanList(BanList.Type.IP).addBan(ipAddress, reason, expiry, sourceLabel);
        for (Player player : Bukkit.getOnlinePlayers()) {
            InetSocketAddress address = player.getAddress();
            if (address == null) {
                continue;
            }
            InetAddress resolved = address.getAddress();
            if (resolved != null && ipAddress.equalsIgnoreCase(resolved.getHostAddress())) {
                player.kickPlayer(reason + "\nBan expires in 1 day.");
            }
        }
        return true;
    }

    private String resolveBanIpTarget(String target) {
        if (this.looksLikeIp(target)) {
            return target;
        }

        Player online = Bukkit.getPlayerExact(target);
        if (online != null && online.getAddress() != null && online.getAddress().getAddress() != null) {
            return online.getAddress().getAddress().getHostAddress();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(target);
        if (offline.isOnline()) {
            Player player = offline.getPlayer();
            if (player != null && player.getAddress() != null && player.getAddress().getAddress() != null) {
                return player.getAddress().getAddress().getHostAddress();
            }
        }

        return null;
    }

    private boolean looksLikeIp(String value) {
        return value != null && (value.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}$") || value.contains(":"));
    }
}
