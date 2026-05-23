package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

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
        if (!(event.getView().getTopInventory().getHolder() instanceof SpellMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.getUniqueId().toString().equals(holder.playerId())) {
            player.sendMessage(ChatColor.RED + "This spell menu is not yours.");
            player.closeInventory();
            return;
        }
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        this.core.getPlayerManaState(player.getUniqueId().toString()).orElseGet(() -> StarterItemUtil.findAnyStarterArchetype(player)
                .map(archetype -> this.core.getOrCreatePlayerManaState(player.getUniqueId().toString(), archetype))
                .orElse(null));
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

        SpellMenuMode mode = holder.mode();
        try {
            if (mode == SpellMenuMode.ASSIGN) {
                if (!this.core.getOrCreateStartingState(player.getUniqueId().toString(), player.getUniqueId().getMostSignificantBits()).unlockedAbilities().contains(spellDefinition.id())) {
                    player.sendMessage(ChatColor.RED + "That spell is not unlocked yet.");
                    return;
                }

                if (event.getClick() == ClickType.LEFT) {
                    this.assignSpellSlot(player, spellDefinition.id(), 0);
                    player.sendMessage(ChatColor.AQUA + "Set " + ChatColor.WHITE + spellDefinition.displayName() + ChatColor.AQUA + " as your primary spell.");
                } else if (event.getClick() == ClickType.RIGHT) {
                    this.assignSpellSlot(player, spellDefinition.id(), 1);
                    player.sendMessage(ChatColor.AQUA + "Set " + ChatColor.WHITE + spellDefinition.displayName() + ChatColor.AQUA + " as your secondary spell.");
                } else {
                    player.sendMessage(ChatColor.GRAY + "Left click for primary. Right click for secondary.");
                }
                new SpellMenu(this.plugin, this.core).open(player, SpellMenuMode.ASSIGN);
            } else {
                PlayerRelicState currentState = this.core.findPlayerState(player.getUniqueId().toString()).orElse(null);
                if (currentState != null) {
                    this.core.savePlayerState(currentState.withPendingRewardSelection(false));
                }
                this.claimRewardSpell(player, spellDefinition.id());
                player.sendMessage(ChatColor.GOLD + "You claimed " + ChatColor.WHITE + spellDefinition.displayName() + ChatColor.GOLD + ".");
                new SpellMenu(this.plugin, this.core).open(player, SpellMenuMode.ASSIGN);
            }
        } catch (IllegalStateException exception) {
            player.sendMessage(ChatColor.RED + exception.getMessage());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof SpellMenuHolder holder) {
            if (holder.mode() == SpellMenuMode.REWARD && event.getPlayer() instanceof Player player) {
                String playerId = player.getUniqueId().toString();
                if (!playerId.equals(holder.playerId())) {
                    return;
                }
                PlayerRelicState state = this.core.findPlayerState(playerId).orElse(null);
                if (state != null && state.pendingRewardSelection()) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        PlayerRelicState refreshed = this.core.findPlayerState(playerId).orElse(null);
                        if (player.isOnline() && refreshed != null && refreshed.pendingRewardSelection()) {
                            new SpellMenu(this.plugin, this.core).open(player, SpellMenuMode.REWARD);
                        }
                    });
                }
            }
        }
    }

    private void claimRewardSpell(Player player, String spellId) {
        String playerId = player.getUniqueId().toString();
        this.core.learnSpell(playerId, spellId);
        PlayerManaState manaState = this.core.getPlayerManaState(playerId).orElse(null);
        if (manaState == null) {
            return;
        }
        if (manaState.equippedSpellIds().size() >= 2 || manaState.equippedSpellIds().contains(spellId)) {
            player.sendMessage(ChatColor.GRAY + "The spell was added to your spellbook. Use /relicboundspells to equip it later.");
            return;
        }
        this.core.equipSpell(playerId, spellId);
        player.sendMessage(ChatColor.GRAY + "The spell was added to your spellbook and equipped automatically.");
    }

    private void assignSpellSlot(Player player, String spellId, int slot) {
        String playerId = player.getUniqueId().toString();
        PlayerManaState manaState = this.core.getPlayerManaState(playerId).orElseThrow(() -> new IllegalStateException("No mana state found for player"));
        List<String> equipped = new ArrayList<>(manaState.equippedSpellIds());
        equipped.remove(spellId);
        while (equipped.size() <= slot) {
            equipped.add("");
        }
        equipped.set(slot, spellId);
        equipped.removeIf(String::isBlank);
        if (equipped.size() > 2) {
            equipped = new ArrayList<>(equipped.subList(0, 2));
        }
        this.core.savePlayerManaState(new PlayerManaState(
                manaState.playerId(),
                manaState.archetype(),
                manaState.currentMana(),
                manaState.maxMana(),
                List.copyOf(equipped),
                manaState.availableScrollIds(),
                manaState.lastManaRegenTime()
        ));
    }
}
