package com.relicbound.paper.anticheat.listener;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.tracking.CombatSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class CombatListener implements Listener {
    private final AnticheatService service;

    public CombatListener(AnticheatService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!this.service.enabled()) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity target)) return;
        long now = System.nanoTime();
        double dx = target.getLocation().getX() - attacker.getLocation().getX();
        double dz = target.getLocation().getZ() - attacker.getLocation().getZ();
        double dy = target.getLocation().getY() - attacker.getLocation().getY();
        double distance = Math.hypot(dx, dz);
        CombatSnapshot snap = new CombatSnapshot(now, attacker.getTicksLived(), target.getUniqueId(), target.getType().name(), distance, 0.0D, Math.abs(attacker.getLocation().getYaw() - target.getLocation().getYaw()), Math.abs(attacker.getLocation().getPitch() - target.getLocation().getPitch()), false, attacker.isSprinting());
        this.service.registry().getOrCreate(attacker).recordCombat(snap);
    }
}