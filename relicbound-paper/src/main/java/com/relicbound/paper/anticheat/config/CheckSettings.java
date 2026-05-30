package com.relicbound.paper.anticheat.config;

public record CheckSettings(
        boolean enabled,
        double threshold,
        double punishThreshold,
        double decayPerSecond,
        double confidenceWeight,
        int alertCooldownSeconds,
        double buffer,
        boolean announceOnPunish
) {
    public static CheckSettings disabled() {
        return new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false);
    }
}