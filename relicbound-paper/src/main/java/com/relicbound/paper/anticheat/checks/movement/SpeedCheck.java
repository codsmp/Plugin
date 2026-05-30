package com.relicbound.paper.anticheat.checks.movement;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.checks.AbstractCheck;
import com.relicbound.paper.anticheat.tracking.PlayerTracker;
import com.relicbound.paper.anticheat.tracking.MovementSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class SpeedCheck extends AbstractCheck {
    public SpeedCheck() { super("speed", "movement", "Detects abnormal ground and air speed.", 1); }

    @Override
    public void runForPlayer(UUID playerId, long nowNanos, AnticheatService service) {
        PlayerTracker tracker = service.registry().get(playerId);
        if (tracker == null) return;
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        var settings = this.settings(service);
        if (!settings.enabled()) return;
        if (tracker.hasAbilityGrace(nowNanos)) return;
        MovementSnapshot latest = tracker.latestMovement();
        if (latest == null) return;
        double horiz = Math.hypot(latest.deltaX(), latest.deltaZ());
        double allowed = player.isSprinting() ? 0.7D : 0.45D;
        allowed += settings.buffer();
        allowed *= 1.0D + (1.0D - service.config().general().tpsFloor());
        if (horiz > allowed) {
            double vl = Math.min(5.0D, (horiz - allowed) * 10.0D);
            this.addViolation(service, playerId, vl, String.format("Speed exceeded: %.3f > %.3f", horiz, allowed), Map.of("horiz", String.valueOf(horiz), "allowed", String.valueOf(allowed)));
            service.confidence().addSignal(playerId, "speed", settings.confidenceWeight(), nowNanos);
        }
    }
}
