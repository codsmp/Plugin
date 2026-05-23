package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class EnchantLimitCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final EnchantLimitStore enchantLimitStore;

    public EnchantLimitCommand(JavaPlugin plugin, EnchantLimitStore enchantLimitStore) {
        this.plugin = plugin;
        this.enchantLimitStore = enchantLimitStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("relicbound.admin.enchantlimit")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to set enchant limits.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /enchantlimit <enchant> <level>");
            return true;
        }

        var enchantmentOptional = EnchantLimitStore.resolveEnchantment(args[0]);
        if (enchantmentOptional.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown enchantment: " + args[0]);
            return true;
        }

        int limit;
        try {
            limit = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "Limit must be a number.");
            return true;
        }

        if (limit < 0) {
            sender.sendMessage(ChatColor.RED + "Limit must be zero or higher.");
            return true;
        }

        Enchantment enchantment = enchantmentOptional.get();
        this.enchantLimitStore.setLimit(enchantment, limit);
        sender.sendMessage(ChatColor.GREEN + "Set " + enchantment.getKey().getKey().toUpperCase(Locale.ROOT) + " limit to " + limit + ".");

        if (sender instanceof Player player) {
            EnchantLimitListener.removeIllegalEnchantments(player, this.enchantLimitStore);
        }
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            EnchantLimitListener.removeIllegalEnchantments(player, this.enchantLimitStore);
        }

        return true;
    }
}