package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ArchetypeSelectionMenu {
    private final RelicboundCore core;

    public ArchetypeSelectionMenu(RelicboundCore core) {
        this.core = core;
    }

    public void open(Player player) {
        Inventory inventory = org.bukkit.Bukkit.createInventory(new ArchetypeSelectionHolder(player.getUniqueId().toString()), 9, ChatColor.DARK_PURPLE + "Choose Your Path");

        ItemStack wand = this.createArchetypeItem(PlayerArchetype.WAND);
        ItemStack staff = this.createArchetypeItem(PlayerArchetype.STAFF);

        inventory.setItem(2, wand);
        inventory.setItem(6, staff);

        player.openInventory(inventory);
    }

    private ItemStack createArchetypeItem(PlayerArchetype archetype) {
        Material material = archetype == PlayerArchetype.WAND ? Material.STICK : Material.BLAZE_ROD;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + archetype.displayName());

        java.util.List<String> lore = new java.util.ArrayList<>();
        if (archetype == PlayerArchetype.WAND) {
            lore.add(ChatColor.AQUA + "Fast spellcasting with low damage");
            lore.add(ChatColor.AQUA + "Better mana efficiency");
            lore.add(ChatColor.AQUA + "Mana regen: 5/sec");
        } else {
            lore.add(ChatColor.RED + "Slow spellcasting with high damage");
            lore.add(ChatColor.RED + "Higher mana consumption");
            lore.add(ChatColor.RED + "Mana regen: 3/sec");
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Click to select");
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
