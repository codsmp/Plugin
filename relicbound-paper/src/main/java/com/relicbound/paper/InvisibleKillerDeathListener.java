package com.relicbound.paper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class InvisibleKillerDeathListener implements Listener {
    private final PaperSpellEngine spellEngine;

    public InvisibleKillerDeathListener(PaperSpellEngine spellEngine) {
        this.spellEngine = spellEngine;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        if (this.spellEngine.isShadowVeiled(killer)) {
            event.setDeathMessage(victim.getName() + " was slain by " + ChatColor.MAGIC + killer.getName() + ChatColor.RESET);
        }
    }
}
