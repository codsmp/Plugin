package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class EnchantLimitListener implements Listener {
    private final EnchantLimitStore enchantLimitStore;

    public EnchantLimitListener(EnchantLimitStore enchantLimitStore) {
        this.enchantLimitStore = enchantLimitStore;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (this.containsDisallowedEnchant(event.getItem().getItemStack())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "That item exceeds the allowed enchant limit.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (this.containsDisallowedEnchant(cursor) || this.containsDisallowedEnchant(current)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "That item exceeds the allowed enchant limit.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (this.containsDisallowedEnchant(event.getOldCursor())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "That item exceeds the allowed enchant limit.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        removeIllegalEnchantments(event.getPlayer(), this.enchantLimitStore);
    }

    private boolean containsDisallowedEnchant(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        return itemStack.getEnchantments().entrySet().stream().anyMatch(entry -> !this.enchantLimitStore.isAllowed(entry.getKey(), entry.getValue()));
    }

    static void removeIllegalEnchantments(Player player, EnchantLimitStore enchantLimitStore) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item != null && containsDisallowedEnchant(item, enchantLimitStore)) {
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                contents[slot] = null;
            }
        }
        inventory.setContents(contents);

        ItemStack[] armorContents = inventory.getArmorContents();
        for (int slot = 0; slot < armorContents.length; slot++) {
            ItemStack item = armorContents[slot];
            if (item != null && containsDisallowedEnchant(item, enchantLimitStore)) {
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                armorContents[slot] = null;
            }
        }
        inventory.setArmorContents(armorContents);

        ItemStack offhand = inventory.getItemInOffHand();
        if (containsDisallowedEnchant(offhand, enchantLimitStore)) {
            player.getWorld().dropItemNaturally(player.getLocation(), offhand.clone());
            inventory.setItemInOffHand(null);
        }
    }

    private static boolean containsDisallowedEnchant(ItemStack itemStack, EnchantLimitStore enchantLimitStore) {
        return itemStack.getEnchantments().entrySet().stream().anyMatch(entry -> !enchantLimitStore.isAllowed(entry.getKey(), entry.getValue()));
    }
}