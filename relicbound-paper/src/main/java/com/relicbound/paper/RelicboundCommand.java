package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class RelicboundCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public RelicboundCommand(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /relicbound <spells|upgrade|grant>");
                return true;
            }
            new RelicMenu(this.core).open(player);
            return true;
        }

        if ("spells".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            new SpellMenu(this.plugin, this.core).open(player);
            return true;
        }

        if ("upgrade".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            try {
                this.core.upgradeTier(player.getUniqueId().toString());
                player.sendMessage(ChatColor.GREEN + "Your relic has advanced.");
                new RelicMenu(this.core).open(player);
            } catch (IllegalStateException exception) {
                player.sendMessage(ChatColor.RED + exception.getMessage());
            }
            return true;
        }

        if ("grant".equalsIgnoreCase(args[0]) && sender.hasPermission("relicbound.admin.grant")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /relicbound grant <player> <essenceType> <amount>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Target player must be online.");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return true;
            }
            this.core.grantEssence(target.getUniqueId().toString(), args[2], amount);
            sender.sendMessage(ChatColor.GREEN + "Granted essence to " + target.getName() + ".");
            target.sendMessage(ChatColor.AQUA + "You received " + amount + " " + args[2] + " essence.");
            return true;
        }

        if (sender instanceof Player player) {
            new RelicMenu(this.core).open(player);
            return true;
        }

        sender.sendMessage("Usage: /relicbound <spells|upgrade|grant>");
        return true;
    }
}
