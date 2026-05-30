package com.relicbound.paper.anticheat.tracking;

import java.util.UUID;

public record CombatSnapshot(
        long nanoTime,
        long tick,
        UUID targetId,
        String targetType,
        double distance,
        double reachAllowance,
        float yawDelta,
        float pitchDelta,
        boolean critical,
        boolean sprinting
) {
}