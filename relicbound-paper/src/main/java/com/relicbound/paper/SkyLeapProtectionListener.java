package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class SkyLeapProtectionListener implements Listener {
    private final PaperSpellEngine spellEngine;

    public SkyLeapProtectionListener(PaperSpellEngine spellEngine) {
        this.spellEngine = spellEngine;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!this.spellEngine.isSkyLeapProtected(player)) {
            return;
        }

        event.setCancelled(true);
    }
}