package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
        if (!(event.getInventory().getHolder() instanceof ArchetypeSelectionHolder holder)) {
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

        String playerId = holder.playerId();
        this.core.getOrCreatePlayerManaState(playerId, archetype);

        Player player = (Player) event.getWhoClicked();
        StarterItemUtil.giveStarterItem(player, archetype);

        int granted = 0;
        for (SpellDefinition spell : this.core.allSpells()) {
            if (spell.requiredTier() != com.relicbound.core.model.RelicTier.TIER_1) {
                continue;
            }
            try {
                this.core.learnSpell(playerId, spell.id());
                this.core.equipSpell(playerId, spell.id());
            } catch (Exception ignored) {
                // Starter loadout should not fail the selection flow.
            }
            granted++;
            if (granted >= 2) {
                break;
            }
        }

        event.getWhoClicked().closeInventory();
        event.getWhoClicked().sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "You have chosen the path of the " + archetype.displayName() + ChatColor.YELLOW + "!");
    }
