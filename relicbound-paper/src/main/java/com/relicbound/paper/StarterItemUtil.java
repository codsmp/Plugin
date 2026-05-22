package com.relicbound.paper;

import com.relicbound.core.model.PlayerArchetype;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public final class StarterItemUtil {
    private StarterItemUtil() {
    }

    public static ItemStack createStarterItem(PlayerArchetype archetype) {
        Material material = archetype == PlayerArchetype.WAND ? Material.STICK : Material.BLAZE_ROD;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + archetype.displayName());
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.AQUA + "A simple " + archetype.displayName() + " to channel your relic.");
            lore.add(ChatColor.GRAY + "Right-click to cast your first spell.");
            lore.add(ChatColor.GRAY + "Shift + right-click to cast your second spell.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isStarterItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        String displayName = meta.getDisplayName();
        return (ChatColor.GOLD + PlayerArchetype.WAND.displayName()).equals(displayName)
                || (ChatColor.GOLD + PlayerArchetype.STAFF.displayName()).equals(displayName);
    }

    public static Optional<PlayerArchetype> inferArchetype(ItemStack item) {
        if (!isStarterItem(item)) {
            return Optional.empty();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return Optional.empty();
        }
        String displayName = meta.getDisplayName();
        if ((ChatColor.GOLD + PlayerArchetype.WAND.displayName()).equals(displayName)) {
            return Optional.of(PlayerArchetype.WAND);
        }
        if ((ChatColor.GOLD + PlayerArchetype.STAFF.displayName()).equals(displayName)) {
            return Optional.of(PlayerArchetype.STAFF);
        }
        return Optional.empty();
    }

    public static Optional<PlayerArchetype> findHeldStarterArchetype(Player player) {
        Optional<PlayerArchetype> mainHand = inferArchetype(player.getInventory().getItemInMainHand());
        if (mainHand.isPresent()) {
            return mainHand;
        }
        return inferArchetype(player.getInventory().getItemInOffHand());
    }

    public static boolean hasStarterItem(Player player, PlayerArchetype archetype) {
        String displayName = ChatColor.GOLD + archetype.displayName();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && displayName.equals(meta.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public static void giveStarterItem(Player player, PlayerArchetype archetype) {
        if (hasStarterItem(player, archetype)) {
            return;
        }

        ItemStack item = createStarterItem(archetype);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        player.sendMessage(ChatColor.AQUA + "You received a starter " + archetype.displayName() + ".");
    }
}