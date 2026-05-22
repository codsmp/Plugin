package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrustCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PlayerTrustStore trustStore;

    public TrustCommand(JavaPlugin plugin, PlayerTrustStore trustStore) {
        this.plugin = plugin;
        this.trustStore = trustStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /trust <player>");
            return true;
        }

        String targetName = args[0];
        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        if (onlineTarget == null) {
            player.sendMessage(ChatColor.RED + "That player must be online to trust them.");
            return true;
        }

        if (onlineTarget.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot trust yourself.");
            return true;
        }

        boolean added = this.trustStore.toggleTrust(player.getUniqueId().toString(), onlineTarget.getUniqueId().toString());
        if (added) {
            player.sendMessage(ChatColor.GREEN + "You now trust " + onlineTarget.getName() + ". Trusted players won't take damage from you.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You no longer trust " + onlineTarget.getName() + ".");
        }
        return true;
    }
}