package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicFamily;
import com.relicbound.core.model.PlayerManaState;
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
        this.open(player, SpellMenuMode.ASSIGN);
    }

    public void open(Player player, SpellMenuMode mode) {
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), player.getUniqueId().getMostSignificantBits());
        SpellMenuHolder holder = new SpellMenuHolder(player.getUniqueId().toString(), mode);
        String title;
        if (mode == SpellMenuMode.REWARD) {
            int remainingSelections = Math.max(1, state.pendingRewardSelections());
            title = ChatColor.DARK_GREEN + (remainingSelections > 1 ? "Choose Your Spells (" + remainingSelections + " left)" : "Choose Your Spell");
        } else {
            title = ChatColor.DARK_BLUE + "Spellbound Arsenal";
        }
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);

        List<SpellDefinition> spells = this.core.allSpells().stream()
                .filter(spell -> {
                    // Reward mode: only show spells the player hasn't unlocked yet
                    if (mode == SpellMenuMode.REWARD && state.unlockedAbilities().contains(spell.id())) return false;
                    // Special-case: Bliss Severance (bliss_egg) requires Dragon Egg in inventory unless already unlocked
                    if ("bliss_egg".equals(spell.id()) && !state.unlockedAbilities().contains(spell.id())) {
                        if (!player.getInventory().contains(Material.DRAGON_EGG)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(SpellDefinition::displayName))
                .toList();
        int slot = 0;
        PlayerManaState manaState = this.core.getPlayerManaState(player.getUniqueId().toString()).orElse(null);
        if (mode == SpellMenuMode.ASSIGN) {
            inventory.setItem(0, createLoadoutSummaryItem("Primary", manaState == null ? null : getEquippedSpell(manaState, 0)));
            inventory.setItem(1, createLoadoutSummaryItem("Secondary", manaState == null ? null : getEquippedSpell(manaState, 1)));
            inventory.setItem(4, createModeInfoItem(ChatColor.BLUE + "Left click"));
            inventory.setItem(5, createModeInfoItem(ChatColor.AQUA + "Right click"));
            slot = 9;
        }
        if (mode == SpellMenuMode.REWARD && spells.isEmpty()) {
            inventory.setItem(22, createEmptyRewardItem());
            player.openInventory(inventory);
            return;
        }
        for (SpellDefinition spell : spells) {
            if (slot >= inventory.getSize()) {
                break;
            }
            boolean unlocked = state.unlockedAbilities().contains(spell.id());
            inventory.setItem(slot++, createSpellItem(spell, unlocked, state, manaState, mode));
        }
        player.openInventory(inventory);
    }

    private ItemStack createSpellItem(SpellDefinition spell, boolean unlocked, PlayerRelicState state, PlayerManaState manaState, SpellMenuMode mode) {
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
        if (mode == SpellMenuMode.ASSIGN) {
            if (!unlocked) {
                lore.add(ChatColor.RED + "Locked until your relic reaches the required tier.");
            } else {
                boolean primary = manaState != null && !manaState.equippedSpellIds().isEmpty() && manaState.equippedSpellIds().get(0).equals(spell.id());
                boolean secondary = manaState != null && manaState.equippedSpellIds().size() > 1 && manaState.equippedSpellIds().get(1).equals(spell.id());
                if (primary) {
                    lore.add(ChatColor.GREEN + "Primary spell.");
                }
                if (secondary) {
                    lore.add(ChatColor.AQUA + "Secondary spell.");
                }
                lore.add(ChatColor.YELLOW + "Left click to make this your primary spell.");
                lore.add(ChatColor.YELLOW + "Right click to make this your secondary spell.");
            }
        } else {
            if (!unlocked) {
                lore.add(ChatColor.RED + "Locked until your relic reaches the required tier.");
            } else {
                lore.add(ChatColor.GREEN + "Click to claim this spell reward.");
            }
        }
        lore.add(ChatColor.DARK_GRAY + "Unlocked spells: " + state.unlockedAbilities().size());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLoadoutSummaryItem(String label, SpellDefinition spell) {
        ItemStack item = new ItemStack(spell == null ? Material.GRAY_DYE : Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + label);
        List<String> lore = new ArrayList<>();
        if (spell == null) {
            lore.add(ChatColor.GRAY + "No spell assigned.");
        } else {
            lore.add(ChatColor.WHITE + spell.displayName());
            lore.add(ChatColor.GRAY + spell.effectType().name());
        }
        meta.setLore(lore);
        if (spell != null && spell.icon().customModelData() != null) {
            meta.setCustomModelData(spell.icon().customModelData());
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createModeInfoItem(String text) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(text);
        meta.setLore(List.of(ChatColor.GRAY + "Use the spell grid below to change your loadout."));
        item.setItemMeta(meta);
        return item;
    }

    private SpellDefinition getEquippedSpell(PlayerManaState manaState, int slot) {
        if (manaState.equippedSpellIds().size() <= slot) {
            return null;
        }
        return this.core.findSpell(manaState.equippedSpellIds().get(slot)).orElse(null);
    }

    private ItemStack createEmptyRewardItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "All spells unlocked");
        meta.setLore(List.of(
                ChatColor.GRAY + "There are no new spells left to claim.",
                ChatColor.GRAY + "Use the spell menu to adjust your primary and secondary spells."
        ));
        item.setItemMeta(meta);
        return item;
    }
}
