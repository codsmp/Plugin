package com.relicbound.paper;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class SpellCombatListener implements Listener {
    private final PaperSpellEngine spellEngine;

    public SpellCombatListener(PaperSpellEngine spellEngine) {
        this.spellEngine = spellEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity livingTarget)) {
            return;
        }
        if (!this.isTool(player.getInventory().getItemInMainHand())) {
            return;
        }
        this.spellEngine.handleLifeDrainHit(player, livingTarget);
    }

    private boolean isTool(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        String materialName = item.getType().name();
        return materialName.endsWith("_SWORD")
            || materialName.endsWith("_AXE")
            || materialName.endsWith("_PICKAXE")
            || materialName.endsWith("_SHOVEL")
            || materialName.endsWith("_HOE")
            || materialName.equals("MACE");
    }
}