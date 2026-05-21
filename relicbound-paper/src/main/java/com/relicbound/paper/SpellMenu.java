package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicFamily;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SpellMenu {
    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public SpellMenu(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    public void open(Player player) {
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), player.getUniqueId().getMostSignificantBits());
        SpellMenuHolder holder = new SpellMenuHolder(player.getUniqueId().toString());
        Inventory inventory = Bukkit.createInventory(holder, 54, ChatColor.DARK_BLUE + "Spellbound Arsenal");
        holder.setInventory(inventory);

        List<SpellDefinition> spells = this.core.allSpells().stream()
                .sorted(Comparator.comparing(SpellDefinition::displayName))
                .toList();
        int slot = 0;
        for (SpellDefinition spell : spells) {
            if (slot >= inventory.getSize()) {
                break;
            }
            boolean unlocked = state.unlockedAbilities().contains(spell.id());
            inventory.setItem(slot++, createSpellItem(spell, unlocked, state));
        }
        player.openInventory(inventory);
    }

    private ItemStack createSpellItem(SpellDefinition spell, boolean unlocked, PlayerRelicState state) {
        ItemStack item = new ItemStack(unlocked ? Material.PAPER : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (spell.icon().customModelData() != null) {
            meta.setCustomModelData(spell.icon().customModelData());
        }
        meta.getPersistentDataContainer().set(SpellMenuKeys.spellIdKey(this.plugin), PersistentDataType.STRING, spell.id());
        meta.setDisplayName((unlocked ? ChatColor.GOLD : ChatColor.DARK_GRAY) + spell.displayName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Effect: " + ChatColor.WHITE + spell.effectType().name());
        lore.add(ChatColor.GRAY + "Tier: " + ChatColor.WHITE + spell.requiredTier().name());
        lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + (spell.cooldownTicks() / 20.0D) + "s");
        lore.add(ChatColor.GRAY + "Mana Cost: " + ChatColor.WHITE + spell.manaCost());
        if (spell.manaPerSecond() > 0) {
            lore.add(ChatColor.GRAY + "Channel Drain: " + ChatColor.WHITE + spell.manaPerSecond() + "/sec");
        }
        lore.add(ChatColor.GRAY + "Model: " + ChatColor.WHITE + spell.modelKey());
        for (RelicFamily family : spell.affinities()) {
            lore.add(ChatColor.GRAY + "Affinity: " + ChatColor.WHITE + family.name());
        }
        lore.add(ChatColor.GRAY + spell.description());
        if (!unlocked) {
            lore.add(ChatColor.RED + "Locked until your relic reaches the required tier.");
        } else {
            lore.add(ChatColor.GREEN + "Click to cast.");
        }
        lore.add(ChatColor.DARK_GRAY + "Unlocked spells: " + state.unlockedAbilities().size());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
