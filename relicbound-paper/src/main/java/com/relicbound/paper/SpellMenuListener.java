package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpellMenuListener implements Listener {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final PaperSpellEngine spellEngine;

    public SpellMenuListener(JavaPlugin plugin, RelicboundCore core, PaperSpellEngine spellEngine) {
        this.plugin = plugin;
        this.core = core;
        this.spellEngine = spellEngine;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SpellMenuHolder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) {
            return;
        }
        String spellId = currentItem.getItemMeta().getPersistentDataContainer().get(SpellMenuKeys.spellIdKey(this.plugin), PersistentDataType.STRING);
        if (spellId == null) {
            return;
        }
        SpellDefinition spellDefinition = this.core.findSpell(spellId).orElse(null);
        if (spellDefinition == null) {
            player.sendMessage(ChatColor.RED + "That spell no longer exists.");
            return;
        }
        try {
            this.spellEngine.cast(player, spellDefinition);
            player.sendMessage(ChatColor.AQUA + "Cast " + ChatColor.WHITE + spellDefinition.displayName() + ChatColor.AQUA + ".");
            new SpellMenu(this.plugin, this.core).open(player);
        } catch (IllegalStateException exception) {
            player.sendMessage(ChatColor.RED + exception.getMessage());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SpellMenuHolder) {
            // no-op
        }
    }
}
