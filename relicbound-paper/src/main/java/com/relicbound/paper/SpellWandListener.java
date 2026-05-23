package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicTier;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        // If both hands hold starter items, only process main hand to avoid duplicate casts.
        if (event.getHand() == EquipmentSlot.OFF_HAND
                && StarterItemUtil.isStarterItem(event.getPlayer().getInventory().getItemInMainHand())) {
            return;
        }
        if (!StarterItemUtil.isStarterItem(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();
        PlayerManaState manaState = this.core.getPlayerManaState(player.getUniqueId().toString())
            .orElseGet(() -> StarterItemUtil.findAnyStarterArchetype(player)
                .map(archetype -> this.core.getOrCreatePlayerManaState(player.getUniqueId().toString(), archetype))
                .orElse(null));
        if (manaState == null) {
            player.sendActionBar(ChatColor.RED + "Choose your archetype first.");
            event.setCancelled(true);
            return;
        }

        manaState = this.ensureStarterLoadout(player, manaState);
        manaState = this.repairUnknownEquipped(player, manaState);

        List<String> equipped = manaState.equippedSpellIds();
        int slot = player.isSneaking() ? 1 : 0;
        if (equipped.size() <= slot) {
            player.sendActionBar(ChatColor.RED + "No " + (slot == 0 ? "primary" : "secondary") + " spell equipped.");
            event.setCancelled(true);
            return;
        }

        SpellDefinition spellDefinition = this.core.findSpell(equipped.get(slot)).orElse(null);
        if (spellDefinition == null) {
            player.sendActionBar(ChatColor.RED + "Invalid equipped spell; repairing loadout...");
            event.setCancelled(true);
            // Try one more repair pass (will notify player if changes applied)
            PlayerManaState repaired = this.repairUnknownEquipped(player, manaState);
            List<String> newEquipped = repaired.equippedSpellIds();
            if (newEquipped.size() > slot) {
                SpellDefinition repairedSpell = this.core.findSpell(newEquipped.get(slot)).orElse(null);
                if (repairedSpell != null) {
                    try {
                        this.spellEngine.cast(player, repairedSpell);
                        player.sendActionBar(ChatColor.AQUA + "Cast " + ChatColor.WHITE + repairedSpell.displayName());
                    } catch (IllegalStateException exception) {
                        player.sendActionBar(ChatColor.RED + exception.getMessage());
                    }
                }
            }
            return;
        }

        // Safety gate: ensure the selected spell is still equipped in the requested slot at cast time.
        List<String> castTimeEquipped = this.core.getPlayerManaState(player.getUniqueId().toString())
                .map(PlayerManaState::equippedSpellIds)
                .orElse(List.of());
        if (castTimeEquipped.size() <= slot || !spellDefinition.id().equals(castTimeEquipped.get(slot))) {
            player.sendActionBar(ChatColor.RED + "Your equipped spell changed. Try casting again.");
            event.setCancelled(true);
            return;
        }

        // Safety gate: ensure slot spell is unlocked before attempting cast.
        boolean unlocked = this.core.findPlayerState(player.getUniqueId().toString())
                .map(state -> state.unlockedAbilities().contains(spellDefinition.id()))
                .orElse(false);
        if (!unlocked) {
            player.sendActionBar(ChatColor.RED + "That spell is not unlocked yet.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        try {
            this.spellEngine.cast(player, spellDefinition);
            player.sendActionBar(ChatColor.AQUA + "Cast " + ChatColor.WHITE + spellDefinition.displayName());
        } catch (IllegalStateException exception) {
            player.sendActionBar(ChatColor.RED + exception.getMessage());
        }
    }

    private PlayerManaState repairUnknownEquipped(Player player, PlayerManaState manaState) {
        this.core.getOrCreateStartingState(
            player.getUniqueId().toString(),
            player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits()
        );

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
        String playerId = player.getUniqueId().toString();
        PlayerRelicState relicState = this.core.getOrCreateStartingState(
                playerId,
                player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits()
        );

        PlayerManaState updated = manaState;
        List<String> unlockedSpellIds = relicState.unlockedAbilities().stream()
                .filter(id -> this.core.findSpell(id).isPresent())
                .toList();

        // Fresh players should get exactly the starter loadout once, not on every cast.
        if (unlockedSpellIds.isEmpty()) {
            StarterLoadoutUtil.grantRandomStarterLoadout(this.core, playerId);
            relicState = this.core.findPlayerState(playerId).orElse(relicState);
            unlockedSpellIds = relicState.unlockedAbilities().stream()
                    .filter(id -> this.core.findSpell(id).isPresent())
                    .toList();
        }

        for (String spellId : unlockedSpellIds) {
            if (updated.equippedSpellIds().size() >= 2) {
                break;
            }
            if (updated.equippedSpellIds().contains(spellId)) {
                continue;
            }
            SpellDefinition spell = this.core.findSpell(spellId).orElse(null);
            if (spell == null || spell.requiredTier() != RelicTier.TIER_1) {
                continue;
            }
            try {
                updated = this.core.equipSpell(playerId, spellId);
            } catch (IllegalStateException ignored) {
                // If a spell cannot be equipped, keep trying remaining unlocked starters.
            }
        }
        return updated;
    }
}