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

        List<String> equipped = manaState.equippedSpellIds();
        int slot = player.isSneaking() ? 1 : 0;
        if (equipped.size() <= slot) {
            player.sendMessage(ChatColor.RED + "You don't have a " + (slot == 0 ? "first" : "second") + " spell equipped yet.");
            event.setCancelled(true);
            return;
        }

        SpellDefinition spellDefinition = this.core.findSpell(equipped.get(slot)).orElse(null);
        if (spellDefinition == null) {
            player.sendMessage(ChatColor.RED + "That equipped spell no longer exists.");
            event.setCancelled(true);
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

    private PlayerManaState ensureStarterLoadout(Player player, PlayerManaState manaState) {
        if (manaState.equippedSpellIds().size() >= 2) {
            return manaState;
        }

        PlayerRelicState relicState = this.core.findPlayerState(player.getUniqueId().toString()).orElse(null);
        if (relicState == null || relicState.unlockedAbilities().isEmpty()) {
            return manaState;
        }

        PlayerManaState updated = manaState;
        int equippedCount = updated.equippedSpellIds().size();
        for (String spellId : relicState.unlockedAbilities()) {
            if (equippedCount >= 2) {
                break;
            }
            if (updated.equippedSpellIds().contains(spellId)) {
                continue;
            }
            try {
                updated = this.core.equipSpell(player.getUniqueId().toString(), spellId);
                equippedCount = updated.equippedSpellIds().size();
            } catch (IllegalStateException ignored) {
                // If a spell cannot be equipped, keep trying unlocked spells.
            }
        }
        return updated;
    }
}