package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;
import com.relicbound.core.model.RelicFamily;

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

        // Give starter item appropriate to archetype
        Player player = (Player) event.getWhoClicked();
        giveStarterForArchetype(player, archetype);

        // Grant two starter spells (first two Tier 1 spells)
        int granted = 0;
        for (SpellDefinition spell : this.core.allSpells()) {
            if (spell.requiredTier() == com.relicbound.core.model.RelicTier.TIER_1) {
                try {
                    this.core.learnSpell(playerId, spell.id());
                    this.core.equipSpell(playerId, spell.id());
                } catch (Exception ignored) {
                }
                granted++;
                if (granted >= 2) break;
            }
        }

        event.getWhoClicked().closeInventory();
        event.getWhoClicked().sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "You have chosen the path of the " + archetype.displayName() + ChatColor.YELLOW + "!");
    }
}

    private void giveStarterForArchetype(Player player, PlayerArchetype archetype) {
        String itemName = ChatColor.GOLD + archetype.displayName();
        boolean hasItem = false;
        for (org.bukkit.inventory.ItemStack it : player.getInventory().getContents()) {
            if (it == null) continue;
            org.bukkit.inventory.meta.ItemMeta m = it.getItemMeta();
            if (m != null && m.hasDisplayName() && itemName.equals(m.getDisplayName())) {
                hasItem = true;
                break;
            }
        }
        if (!hasItem) {
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(archetype == PlayerArchetype.WAND ? org.bukkit.Material.STICK : org.bukkit.Material.BLAZE_ROD);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(itemName);
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.AQUA + "A simple " + archetype.displayName() + " to channel your relic.");
                lore.add(ChatColor.GRAY + "Right-click to cast spells.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(ChatColor.AQUA + "You received a starter " + archetype.displayName() + ".");
        }
    }
