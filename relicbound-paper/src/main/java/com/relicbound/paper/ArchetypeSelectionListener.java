package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class ArchetypeSelectionListener implements Listener {
    private final RelicboundCore core;

    public ArchetypeSelectionListener(RelicboundCore core) {
        this.core = core;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ArchetypeSelectionHolder)) {
            return;
        }

        event.setCancelled(true);

        ArchetypeSelectionHolder holder = (ArchetypeSelectionHolder) event.getInventory().getHolder();
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

        String playerId = holder.playerId();
        var manaState = this.core.getOrCreatePlayerManaState(playerId, archetype);
        this.core.savePlayerManaState(manaState);

        event.getWhoClicked().closeInventory();
        event.getWhoClicked().sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "You have chosen the path of the " + archetype.displayName() + ChatColor.YELLOW + "!");
    }
}
