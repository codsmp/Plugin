package com.relicbound.paper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class TrustDamageListener implements Listener {
    private final PlayerTrustStore trustStore;
    private final PlayerTeamStore teamStore;

    public TrustDamageListener(PlayerTrustStore trustStore, PlayerTeamStore teamStore) {
        this.trustStore = trustStore;
        this.teamStore = teamStore;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = this.resolveAttacker(event.getDamager());
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        if (this.trustStore.isTrusted(attacker.getUniqueId().toString(), victim.getUniqueId().toString())
            || this.teamStore.isAlliedOrSame(attacker.getUniqueId().toString(), victim.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}