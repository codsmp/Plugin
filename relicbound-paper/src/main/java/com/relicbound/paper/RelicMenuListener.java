package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class RelicMenuListener implements Listener {
    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public RelicMenuListener(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof RelicMenuHolder) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player && event.getCurrentItem() != null) {
                Material type = event.getCurrentItem().getType();
                if (type == Material.ANVIL) {
                    try {
                        PlayerRelicState upgraded = this.core.upgradeTier(player.getUniqueId().toString());
                        player.sendMessage(ChatColor.GREEN + "Your relic grows stronger.");
                        boolean hasLockedSpells = this.core.allSpells().stream().anyMatch(spell -> !upgraded.unlockedAbilities().contains(spell.id()));
                        if (hasLockedSpells) {
                            SpellSelectionSession.beginRewardSelection(player.getUniqueId().toString());
                            new SpellMenu(this.plugin, this.core).open(player, SpellMenuMode.REWARD);
                        } else {
                            player.sendMessage(ChatColor.GRAY + "You already have every spell unlocked.");
                            new RelicMenu(this.core).open(player);
                        }
                    } catch (IllegalStateException exception) {
                        player.sendMessage(ChatColor.RED + exception.getMessage());
                    }
                }
                if (type == Material.ENCHANTED_BOOK) {
                    new SpellMenu(this.plugin, this.core).open(player);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RelicMenuHolder) {
            // No-op for now; menu is informational.
        }
    }
}
