package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RelicResetCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PaperPlatformAdapter adapter;
    private final PlayerTrustStore trustStore;

    public RelicResetCommand(JavaPlugin plugin, PaperPlatformAdapter adapter, PlayerTrustStore trustStore) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.trustStore = trustStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("relicbound.admin.reset")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to reset the server data.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "[Relicbound] Resetting all Relicbound data now...");
        if (this.plugin instanceof RelicboundPaperPlugin relicboundPlugin) {
            relicboundPlugin.executeFullReset(sender.getName());
        } else {
            this.adapter.resetPersistentState();
            this.trustStore.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.RED + "Relicbound data has been reset. Please reconnect.");
            }
            this.plugin.getLogger().warning("Relicbound data was reset by " + sender.getName());
        }
        return true;
    }
}