package com.relicbound.paper.anticheat.listener;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.tracking.EnvironmentFlags;
import com.relicbound.paper.anticheat.tracking.MovementSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MovementListener implements Listener {
    private final AnticheatService service;

    public MovementListener(AnticheatService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!this.service.enabled()) return;
        var player = event.getPlayer();
        long now = System.nanoTime();
        var from = event.getFrom();
        var to = event.getTo();
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        MovementSnapshot snap = new MovementSnapshot(now, player.getTicksLived(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch(), dx, dy, dz, player.isOnGround(), player.isSprinting(), player.isSneaking(), player.isInsideVehicle(), new EnvironmentFlags(false, player.isSwimming(), false, false, false, false, false, false, false, false, player.isOnGround()));
        this.service.registry().getOrCreate(player).recordMovement(snap);
    }
}