package com.relicbound.paper.anticheat.checks.combat;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.checks.AbstractCheck;
import com.relicbound.paper.anticheat.tracking.PlayerTracker;
import com.relicbound.paper.anticheat.tracking.CombatSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class ReachCheck extends AbstractCheck {
    public ReachCheck() { super("reach", "combat", "Detects extended reach and suspicious attack distances.", 1); }

    @Override
    public void runForPlayer(UUID playerId, long nowNanos, AnticheatService service) {
        PlayerTracker tracker = service.registry().get(playerId);
        if (tracker == null) return;
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        var settings = this.settings(service);
        if (!settings.enabled()) return;
        CombatSnapshot latest = tracker.latestCombat();
        if (latest == null) return;
        double measured = latest.distance();
        double base = 3.0D;
        double allowance = settings.buffer();
        double allowed = base + allowance + (player.isSprinting() ? 0.2D : 0.0D);
        if (measured > allowed) {
            double vl = Math.min(6.0D, (measured - allowed) * 8.0D);
            this.addViolation(service, playerId, vl, String.format("Reach exceeded: %.3f > %.3f", measured, allowed), Map.of("measured", String.valueOf(measured), "allowed", String.valueOf(allowed)));
            service.confidence().addSignal(playerId, "reach", settings.confidenceWeight(), nowNanos);
        }
    }
}
