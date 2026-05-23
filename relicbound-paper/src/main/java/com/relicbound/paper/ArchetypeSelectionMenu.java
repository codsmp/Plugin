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
    private static final int WAND_CUSTOM_MODEL_DATA = 5001;
    private static final int STAFF_CUSTOM_MODEL_DATA = 5002;

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
        if (archetype == PlayerArchetype.WAND) {
            meta.setCustomModelData(WAND_CUSTOM_MODEL_DATA);
        } else {
            meta.setCustomModelData(STAFF_CUSTOM_MODEL_DATA);
        }

        java.util.List<String> lore = new java.util.ArrayList<>();
        if (archetype == PlayerArchetype.WAND) {
            lore.add(ChatColor.AQUA + "Fast casting and better mana efficiency");
            lore.add(ChatColor.AQUA + "Best for mobility and quick combos");
            lore.add(ChatColor.AQUA + "Mana regen: 5/sec");
        } else {
            lore.add(ChatColor.RED + "Slower casting with higher damage");
            lore.add(ChatColor.RED + "Stronger spells and heavier impact");
            lore.add(ChatColor.RED + "Mana regen: 3/sec");
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Click to select");
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
