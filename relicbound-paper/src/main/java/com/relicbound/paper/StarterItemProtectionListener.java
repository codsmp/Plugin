package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public final class StarterItemProtectionListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!StarterItemUtil.isStarterItem(event.getItemDrop().getItemStack())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "[Relicbound] Starter wands cannot be dropped.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClick() != ClickType.DROP && event.getClick() != ClickType.CONTROL_DROP) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (!StarterItemUtil.isStarterItem(item)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "[Relicbound] Starter wands cannot be dropped.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        boolean blocked = false;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (StarterItemUtil.isStarterItem(item)) {
                blocked = true;
                break;
            }
        }

        if (!blocked) {
            return;
        }

        event.getInventory().setResult(null);
        for (Player viewer : event.getViewers()) {
            viewer.sendMessage(ChatColor.RED + "[Relicbound] Starter wands cannot be used in crafting.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(StarterItemUtil::isStarterItem);
    }
}