package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public final class SpellWandListener implements Listener {
    private final RelicboundCore core;
    private final PaperSpellEngine spellEngine;

    public SpellWandListener(RelicboundCore core, PaperSpellEngine spellEngine) {
        this.core = core;
        this.spellEngine = spellEngine;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!StarterItemUtil.isStarterItem(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();
        PlayerManaState manaState = this.core.getPlayerManaState(player.getUniqueId().toString())
                .orElse(null);
        if (manaState == null) {
            player.sendMessage(ChatColor.RED + "Choose your archetype first.");
            event.setCancelled(true);
            return;
        }

        manaState = this.ensureStarterLoadout(player, manaState);
        manaState = this.repairUnknownEquipped(player, manaState);

        List<String> equipped = manaState.equippedSpellIds();
        int slot = player.isSneaking() ? 1 : 0;
        if (equipped.size() <= slot) {
            player.sendMessage(ChatColor.RED + "You don't have a " + (slot == 0 ? "first" : "second") + " spell equipped yet.");
            event.setCancelled(true);
            return;
        }

        SpellDefinition spellDefinition = this.core.findSpell(equipped.get(slot)).orElse(null);
        if (spellDefinition == null) {
            player.sendMessage(ChatColor.RED + "An equipped spell appears invalid; attempting to repair your loadout now.");
            event.setCancelled(true);
            // Try one more repair pass (will notify player if changes applied)
            PlayerManaState repaired = this.repairUnknownEquipped(player, manaState);
            List<String> newEquipped = repaired.equippedSpellIds();
            if (newEquipped.size() > slot) {
                SpellDefinition repairedSpell = this.core.findSpell(newEquipped.get(slot)).orElse(null);
                if (repairedSpell != null) {
                    try {
                        this.spellEngine.cast(player, repairedSpell);
                        player.sendMessage(ChatColor.AQUA + "Cast " + repairedSpell.displayName());
                    } catch (IllegalStateException exception) {
                        player.sendMessage(ChatColor.RED + exception.getMessage());
                    }
                }
            }
            return;
        }

        event.setCancelled(true);
        try {
            this.spellEngine.cast(player, spellDefinition);
            player.sendMessage(ChatColor.AQUA + "Cast " + spellDefinition.displayName());
        } catch (IllegalStateException exception) {
            player.sendMessage(ChatColor.RED + exception.getMessage());
        }
    }

    private PlayerManaState repairUnknownEquipped(Player player, PlayerManaState manaState) {
        // If any equipped id no longer exists in the catalog, try to replace it with a valid starter spell.
        List<String> equipped = manaState.equippedSpellIds();
        boolean changed = false;
        PlayerManaState updated = manaState;

        for (int i = 0; i < Math.max(equipped.size(), 2); i++) {
            if (i >= updated.equippedSpellIds().size()) {
                // slot empty, try to equip a starter
                for (SpellDefinition candidate : this.core.allSpells()) {
                    if (candidate.requiredTier() != com.relicbound.core.model.RelicTier.TIER_1) continue;
                    if (updated.equippedSpellIds().contains(candidate.id())) continue;
                    try {
                        // learn if necessary, then equip
                        this.core.learnSpell(player.getUniqueId().toString(), candidate.id());
                        updated = this.core.equipSpell(player.getUniqueId().toString(), candidate.id());
                        changed = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }
                continue;
            }

            String id = updated.equippedSpellIds().get(i);
            if (this.core.findSpell(id).isPresent()) {
                continue;
            }

            // Unknown id: try to replace with a starter spell
            for (SpellDefinition candidate : this.core.allSpells()) {
                if (candidate.requiredTier() != com.relicbound.core.model.RelicTier.TIER_1) continue;
                if (updated.equippedSpellIds().contains(candidate.id())) continue;
                try {
                    this.core.learnSpell(player.getUniqueId().toString(), candidate.id());
                    updated = this.core.equipSpell(player.getUniqueId().toString(), candidate.id());
                    changed = true;
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        if (changed) {
            player.sendMessage(org.bukkit.ChatColor.AQUA + "[Relicbound] Repaired your equipped spells to a valid starter loadout.");
        }
        return updated;
    }

    private PlayerManaState ensureStarterLoadout(Player player, PlayerManaState manaState) {
        PlayerRelicState relicState = this.core.findPlayerState(player.getUniqueId().toString()).orElse(null);
        if (relicState == null) {
            return manaState;
        }

        PlayerManaState updated = manaState;
        int starterSpellCount = 0;
        for (SpellDefinition spell : this.core.allSpells()) {
            if (spell.requiredTier() != com.relicbound.core.model.RelicTier.TIER_1) {
                continue;
            }
            if (starterSpellCount >= 2) {
                break;
            }
            starterSpellCount++;
            if (!relicState.unlockedAbilities().contains(spell.id())) {
                try {
                    this.core.learnSpell(player.getUniqueId().toString(), spell.id());
                } catch (IllegalStateException ignored) {
                    // If we cannot learn the spell yet, keep scanning for another starter option.
                    continue;
                }
            }
            if (updated.equippedSpellIds().contains(spell.id())) {
                continue;
            }
            try {
                updated = this.core.equipSpell(player.getUniqueId().toString(), spell.id());
            } catch (IllegalStateException ignored) {
                // If a spell cannot be equipped, keep trying other starter spells.
            }
        }
        return updated;
    }
}