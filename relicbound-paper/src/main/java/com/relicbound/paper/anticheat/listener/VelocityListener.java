package com.relicbound.paper.anticheat.listener;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.tracking.VelocitySnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public final class VelocityListener implements Listener {
    private final AnticheatService service;

    public VelocityListener(AnticheatService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!this.service.enabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        // Record velocity attempts conservatively when player is damaged
        VelocitySnapshot snap = new VelocitySnapshot(System.nanoTime(), player.getTicksLived(), player.getVelocity().getX(), player.getVelocity().getY(), player.getVelocity().getZ(), event.getCause().name());
        this.service.registry().getOrCreate(player).recordVelocity(snap);
    }
}