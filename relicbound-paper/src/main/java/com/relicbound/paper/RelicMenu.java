package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class RelicMenu {
    private final RelicboundCore core;

    public RelicMenu(RelicboundCore core) {
        this.core = core;
    }

    public void open(Player player) {
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), player.getUniqueId().getMostSignificantBits());
        RelicDefinition definition = this.core.registryLookup(state.relicId()).orElseThrow();
        RelicMenuHolder holder = new RelicMenuHolder(player.getUniqueId().toString());
        Inventory inventory = Bukkit.createInventory(holder, 27, ChatColor.DARK_PURPLE + "Relicbound");
        holder.setInventory(inventory);

        inventory.setItem(11, createInfoItem(definition, state));
        inventory.setItem(13, createEssenceItem(state));
        inventory.setItem(15, createUpgradeItem(state));
        inventory.setItem(22, createSpellbookItem(state));
        player.openInventory(inventory);
    }

    private ItemStack createInfoItem(RelicDefinition definition, PlayerRelicState state) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + definition.displayName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Family: " + ChatColor.WHITE + definition.family().name());
        lore.add(ChatColor.GRAY + "Rarity: " + ChatColor.WHITE + definition.rarity().name());
        lore.add(ChatColor.GRAY + "Tier: " + ChatColor.WHITE + state.tier().name());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEssenceItem(PlayerRelicState state) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Essence");
        meta.setLore(List.of(
                ChatColor.GRAY + "Stored: " + ChatColor.WHITE + state.currentEssence(),
                ChatColor.GRAY + "Types: " + ChatColor.WHITE + state.essenceByType().size()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUpgradeItem(PlayerRelicState state) {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Upgrade Tier");
        meta.setLore(List.of(
                ChatColor.GRAY + "Use essence to evolve your relic.",
                ChatColor.GRAY + "Current tier: " + ChatColor.WHITE + state.tier().name()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSpellbookItem(PlayerRelicState state) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Spellbook");
        meta.setLore(List.of(
                ChatColor.GRAY + "Open the spell arsenal.",
                ChatColor.GRAY + "Unlocked spells: " + ChatColor.WHITE + state.unlockedAbilities().size()
        ));
        item.setItemMeta(meta);
        return item;
    }
}
