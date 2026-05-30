package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class GuideMenu {
    private final org.bukkit.plugin.java.JavaPlugin plugin;

    public GuideMenu(org.bukkit.plugin.java.JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        GuideMenuHolder holder = new GuideMenuHolder(player.getUniqueId().toString());
                Inventory inventory = Bukkit.createInventory(holder, 27, ChatColor.DARK_AQUA + "Witch Guide");
        holder.setInventory(inventory);

        inventory.setItem(10, pageItem(Material.STICK, ChatColor.GOLD + "Fast Spellcaster Wand", List.of(
                ChatColor.GRAY + "Fast casting and lower mana use.",
                ChatColor.GRAY + "Best for mobility and quick combos.",
                ChatColor.GRAY + "Starter wand model is protected and cannot be dropped."
        )));
        inventory.setItem(12, pageItem(Material.BLAZE_ROD, ChatColor.RED + "Heavy Spellcaster Staff", List.of(
                ChatColor.GRAY + "Slower casting with stronger hits.",
                ChatColor.GRAY + "Costs more mana and rewards deliberate play."
        )));
        inventory.setItem(14, pageItem(Material.ENCHANTED_BOOK, ChatColor.AQUA + "Spell Menu", List.of(
                ChatColor.GRAY + "/witchspells opens your spellbook.",
                ChatColor.GRAY + "Left click to set primary, right click for secondary.",
                ChatColor.GRAY + "Rewards always ignore spells you already own."
        )));
        inventory.setItem(16, pageItem(Material.ANVIL, ChatColor.GREEN + "Progression", List.of(
                ChatColor.GRAY + "Earn essence to upgrade your relic tier.",
                ChatColor.GRAY + "More tier = more unlocks = stronger build options."
        )));
        inventory.setItem(22, pageItem(Material.PAPER, ChatColor.YELLOW + "Controls", List.of(
                ChatColor.GRAY + "Right-click to cast primary spell.",
                ChatColor.GRAY + "Shift + right-click to cast secondary spell.",
                ChatColor.GRAY + "Use /trust to mark allies as safe from your damage."
        )));

        player.openInventory(inventory);
    }

    private ItemStack pageItem(Material material, String title, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}