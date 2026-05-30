package com.relicbound.paper.anticheat.checks.movement;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.checks.AbstractCheck;
import com.relicbound.paper.anticheat.tracking.MovementSnapshot;
import com.relicbound.paper.anticheat.tracking.PlayerTracker;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlyCheck extends AbstractCheck {
    private static final double MIN_AIR_TIME_SECONDS = 1.65D;
    private static final int MIN_AIRBORNE_SAMPLES = 8;
    private static final int MAX_AIRBORNE_SAMPLES = 18;
    private static final long VELOCITY_GRACE_NANOS = 500_000_000L;
    private static final long TELEPORT_GRACE_NANOS = 800_000_000L;

    public FlyCheck() {
        super("fly", "movement", "Detects sustained unsupported flight.", 1);
    }

    @Override
    public void runForPlayer(UUID playerId, long nowNanos, AnticheatService service) {
        PlayerTracker tracker = service.registry().get(playerId);
        if (tracker == null) {
            return;
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }

        var settings = this.settings(service);
        if (!settings.enabled()) {
            return;
        }
        if (tracker.hasAbilityGrace(nowNanos)) {
            return;
        }
        if (player.isInsideVehicle() || player.isGliding() || player.isSwimming()) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.getAllowFlight()) {
            return;
        }
        if (nowNanos - tracker.lastVelocityNanoTime() < VELOCITY_GRACE_NANOS) {
            return;
        }
        if (nowNanos - tracker.lastTeleportNanoTime() < TELEPORT_GRACE_NANOS) {
            return;
        }

        MovementSnapshot latest = tracker.latestMovement();
        if (latest == null || latest.onGround()) {
            return;
        }
        if (latest.environment().inLiquid() || latest.environment().inWater() || latest.environment().inLava()
                || latest.environment().onLadder() || latest.environment().onVine() || latest.environment().onHoney()
                || latest.environment().onSlime() || latest.environment().inCobweb() || latest.environment().inPowderSnow()
                || latest.environment().onGroundLike()) {
            return;
        }

        long groundNano = tracker.lastGroundNanoTime();
        if (groundNano <= 0L) {
            return;
        }

        double airTimeSeconds = (nowNanos - groundNano) / 1_000_000_000.0D;
        if (airTimeSeconds < MIN_AIR_TIME_SECONDS) {
            return;
        }

        int airborneSamples = 0;
        for (int index = tracker.movements().size() - 1; index >= 0; index--) {
            MovementSnapshot sample = tracker.movements().get(index);
            if (sample == null || sample.onGround()) {
                break;
            }
            if (sample.environment().inLiquid() || sample.environment().inWater() || sample.environment().inLava()
                    || sample.environment().onLadder() || sample.environment().onVine() || sample.environment().onHoney()
                    || sample.environment().onSlime() || sample.environment().inCobweb() || sample.environment().inPowderSnow()
                    || sample.environment().onGroundLike()) {
                break;
            }
            airborneSamples++;
            if (airborneSamples >= MAX_AIRBORNE_SAMPLES) {
                break;
            }
        }
        if (airborneSamples < MIN_AIRBORNE_SAMPLES) {
            return;
        }

        double deltaY = latest.deltaY();
        if (deltaY < -0.18D) {
            return;
        }

        double hoverScore = Math.max(0.0D, 0.035D - Math.abs(deltaY)) * 18.0D;
        double riseScore = Math.max(0.0D, deltaY) * 10.0D;
        double airScore = Math.max(0.0D, airTimeSeconds - MIN_AIR_TIME_SECONDS) * 2.8D;
        double sampleScore = Math.max(0.0D, airborneSamples - MIN_AIRBORNE_SAMPLES) * 0.45D;
        double amount = airScore + hoverScore + riseScore + sampleScore;

        if (amount < settings.threshold()) {
            return;
        }

        Map<String, String> evidence = new HashMap<>();
        evidence.put("airTimeSeconds", String.valueOf(airTimeSeconds));
        evidence.put("deltaY", String.valueOf(deltaY));
        evidence.put("airborneSamples", String.valueOf(airborneSamples));
        evidence.put("amount", String.valueOf(amount));

        this.addViolation(service, playerId, amount, String.format("Unsupported flight detected: air=%.2fs dy=%.3f samples=%d", airTimeSeconds, deltaY, airborneSamples), evidence);
        service.confidence().addSignal(playerId, "fly", settings.confidenceWeight(), nowNanos);
    }
}
