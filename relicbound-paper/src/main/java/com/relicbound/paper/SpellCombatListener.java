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

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity livingTarget)) {
            return;
        }
        if (!this.isCriticalHit(player)) {
            return;
        }

        this.spellEngine.handleLifeDrainCriticalHit(player, livingTarget);
    }

    private boolean isCriticalHit(Player player) {
        return player.getFallDistance() > 0.0F
                && !player.isOnGround()
                && !player.isInWater()
                && !player.isInsideVehicle()
                && !player.isSwimming();
    }
}