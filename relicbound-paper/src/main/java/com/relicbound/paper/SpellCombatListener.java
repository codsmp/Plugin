package com.relicbound.paper;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
        this.spellEngine.handleLifeDrainCriticalHit(player, livingTarget);
    }
}