package com.relicbound.paper.anticheat.command;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.tracking.PlayerTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class AnticheatCommand implements CommandExecutor {
    private final AnticheatService service;

    public AnticheatCommand(AnticheatService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Witch Anticheat Commands: /ac debug <player> | /ac violations <player> | /ac reload");
            return true;
        }
        String sub = args[0].toLowerCase(java.util.Locale.ROOT);
        if (sub.equals("reload")) {
            if (!sender.hasPermission("witch.admin.debug")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to reload the anticheat config.");
                return true;
            }
            this.service.reload();
            sender.sendMessage(ChatColor.GREEN + "Anticheat config reloaded.");
            return true;
        }

        if (sub.equals("debug") || sub.equals("profile")) {
            if (!sender.hasPermission("witch.admin.debug")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to debug players.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /ac debug <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Target player must be online.");
                return true;
            }
            UUID id = target.getUniqueId();
            PlayerTracker t = this.service.registry().getOrCreate(target);
            sender.sendMessage(ChatColor.GOLD + "Anticheat Debug: " + ChatColor.YELLOW + target.getName());
            sender.sendMessage(ChatColor.GRAY + "Recent movements: " + ChatColor.WHITE + t.movements().size());
            sender.sendMessage(ChatColor.GRAY + "Recent clicks: " + ChatColor.WHITE + t.clicks().size());
            sender.sendMessage(ChatColor.GRAY + "Recent combat hits: " + ChatColor.WHITE + t.combat().size());
            sender.sendMessage(ChatColor.GRAY + "Current VL: " + ChatColor.WHITE + this.service.violations().totalVl(id));
            sender.sendMessage(ChatColor.GRAY + "Current Confidence: " + ChatColor.WHITE + this.service.confidence().confidence(id));
            return true;
        }

        if (sub.equals("violations")) {
            if (!sender.hasPermission("witch.admin.debug")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to view violations.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /ac violations <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Target player must be online.");
                return true;
            }
            UUID id = target.getUniqueId();
            sender.sendMessage(ChatColor.GOLD + "Anticheat Violations for " + ChatColor.YELLOW + target.getName());
            sender.sendMessage(ChatColor.GRAY + "Total VL: " + ChatColor.WHITE + this.service.violations().totalVl(id));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /ac <debug|violations|reload>");
        return true;
    }
}