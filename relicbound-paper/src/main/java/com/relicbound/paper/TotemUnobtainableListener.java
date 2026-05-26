package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class TotemUnobtainableListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        stripTotems(event.getDrops());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        stripTotems(event.getDrops());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        Item item = event.getItem();
        if (item.getItemStack().getType() != Material.TOTEM_OF_UNDYING) {
            return;
        }

        event.setCancelled(true);
        item.remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        purgePlayerInventory(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        purgePlayerInventory(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        boolean stripped = stripTotems(inventory.getContents());
        if (stripped && event.getPlayer() instanceof Player player) {
            player.sendMessage(ChatColor.RED + "[Relicbound] Totems of Undying are disabled on this server.");
        }
    }

    private void purgePlayerInventory(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        boolean stripped = stripTotems(player.getInventory());
        if (player.getInventory().getItemInOffHand() != null
                && player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
            player.getInventory().setItemInOffHand(null);
            stripped = true;
        }
        if (stripped) {
            player.sendMessage(ChatColor.RED + "[Relicbound] Totems of Undying are disabled on this server.");
        }
    }

    private boolean stripTotems(Inventory inventory) {
        if (inventory == null) {
            return false;
        }

        boolean changed = false;
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) {
                continue;
            }

            inventory.setItem(i, null);
            changed = true;
        }
        return changed;
    }
}