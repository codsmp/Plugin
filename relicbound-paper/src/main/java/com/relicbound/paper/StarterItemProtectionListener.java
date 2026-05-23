package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StarterItemProtectionListener implements Listener {
    private final Map<UUID, List<ItemStack>> pendingDeathRestore = new ConcurrentHashMap<>();

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

        // Block dropping starter items by clicking outside inventory with a starter item on cursor.
        if (event.getClickedInventory() == null
                && (event.getAction() == InventoryAction.DROP_ALL_CURSOR || event.getAction() == InventoryAction.DROP_ONE_CURSOR)
                && StarterItemUtil.isStarterItem(event.getCursor())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[Relicbound] Starter wands cannot be dropped.");
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
        for (HumanEntity viewer : event.getViewers()) {
            if (viewer instanceof Player player) {
                player.sendMessage(ChatColor.RED + "[Relicbound] Starter wands cannot be used in crafting.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
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

        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            player.sendMessage(ChatColor.RED + "[Relicbound] Starter wands cannot be used in crafting.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        List<ItemStack> retained = new ArrayList<>();
        event.getDrops().removeIf(item -> {
            if (!StarterItemUtil.isStarterItem(item)) {
                return false;
            }
            retained.add(item.clone());
            return true;
        });

        if (!retained.isEmpty()) {
            this.pendingDeathRestore.put(event.getEntity().getUniqueId(), retained);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        this.restorePendingStarterItems(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.restorePendingStarterItems(event.getPlayer());
    }

    private void restorePendingStarterItems(Player player) {
        List<ItemStack> pending = this.pendingDeathRestore.remove(player.getUniqueId());
        if (pending == null || pending.isEmpty()) {
            return;
        }

        List<ItemStack> stillPending = new ArrayList<>();
        for (ItemStack item : pending) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
            stillPending.addAll(leftovers.values());
        }

        if (!stillPending.isEmpty()) {
            this.pendingDeathRestore.put(player.getUniqueId(), stillPending);
            player.sendMessage(ChatColor.YELLOW + "[Relicbound] Your starter item will be restored when you have inventory space.");
            return;
        }

        player.sendMessage(ChatColor.AQUA + "[Relicbound] Your starter item was kept through death.");
    }
}