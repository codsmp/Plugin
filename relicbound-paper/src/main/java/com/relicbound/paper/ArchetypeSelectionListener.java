package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArchetypeSelectionListener implements Listener {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final ArchetypeSelectionMenu menu;

    public ArchetypeSelectionListener(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
        this.menu = new ArchetypeSelectionMenu(core);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ArchetypeSelectionHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();
        PlayerArchetype archetype;
        if (slot == 2) {
            archetype = PlayerArchetype.WAND;
        } else if (slot == 6) {
            archetype = PlayerArchetype.STAFF;
        } else {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String playerId = player.getUniqueId().toString();
        if (!playerId.equals(holder.playerId())) {
            player.sendMessage(ChatColor.RED + "This archetype menu is not yours.");
            player.closeInventory();
            return;
        }

        this.core.getOrCreatePlayerManaState(playerId, archetype);

        StarterItemUtil.giveStarterItem(player, archetype);

        StarterLoadoutUtil.grantRandomStarterLoadout(this.core, playerId);

        event.getWhoClicked().closeInventory();
        event.getWhoClicked().sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "You have chosen the path of the " + archetype.displayName() + ChatColor.YELLOW + "!");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ArchetypeSelectionHolder holder)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String playerId = player.getUniqueId().toString();
        if (!playerId.equals(holder.playerId())) {
            return;
        }

        if (this.core.getPlayerManaState(playerId).isPresent()) {
            return;
        }

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (this.core.getPlayerManaState(playerId).isPresent()) {
                return;
            }
            this.menu.open(player);
            player.sendMessage(ChatColor.YELLOW + "Choose Wand or Staff to begin casting spells.");
        });
    }
}
