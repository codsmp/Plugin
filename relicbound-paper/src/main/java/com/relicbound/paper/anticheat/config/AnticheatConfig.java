package com.relicbound.paper.anticheat.config;

import java.util.LinkedHashMap;
import java.util.Map;

public record AnticheatConfig(
        boolean enabled,
        GeneralSettings general,
        AlertSettings alerts,
        AnnouncementSettings announcements,
        LoggingSettings logging,
        PunishmentSettings punishments,
        TrackingSettings tracking,
        Map<String, CheckSettings> checks
) {
    public CheckSettings check(String key) {
        return this.checks.getOrDefault(key, CheckSettings.disabled());
    }

    public static AnticheatConfig defaults() {
        Map<String, CheckSettings> checks = new LinkedHashMap<>();
        checks.put("reach", new CheckSettings(true, 8.5D, 16.0D, 0.10D, 10.0D, 3, 0.25D, true));
        checks.put("killaura", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("aimassist", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("autoclicker", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("velocity", new CheckSettings(true, 8.0D, 16.0D, 0.10D, 10.0D, 3, 0.35D, true));
        checks.put("speed", new CheckSettings(true, 100.0D, 150.0D, 0.10D, 9.0D, 3, 0.0D, true));
        checks.put("fly", new CheckSettings(true, 12.0D, 24.0D, 0.12D, 14.0D, 2, 0.20D, true));
        checks.put("nofall", new CheckSettings(true, 8.0D, 16.0D, 0.08D, 8.0D, 3, 0.20D, false));
        checks.put("jesus", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("step", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("spider", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("timer", new CheckSettings(true, 8.0D, 16.0D, 0.10D, 10.0D, 3, 0.25D, true));
        checks.put("phase", new CheckSettings(true, 10.0D, 20.0D, 0.06D, 10.0D, 3, 0.25D, true));
        checks.put("fastbreak", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("nuker", new CheckSettings(true, 10.0D, 20.0D, 0.08D, 8.0D, 3, 0.25D, false));
        checks.put("scaffold", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));
        checks.put("interaction", new CheckSettings(false, 0.0D, 0.0D, 0.0D, 0.0D, 0, 0.0D, false));

        return new AnticheatConfig(
                true,
                new GeneralSettings(true, 20, 30, 0.75D, 0.50D),
                new AlertSettings(true, 3, true, "witchsmp.anticheat.alerts"),
                new AnnouncementSettings(true, 85.0D, 30, true, "", "⚡ Witch SMP Anticheat ⚡"),
                new LoggingSettings(true, true, true, true, true),
                new PunishmentSettings(true, PunishmentAction.KICK, 99.0D, "minecraft:kick {player} Cheating detected by Witch SMP Anticheat.", "", "", 160),
                new TrackingSettings(64, 64, 64, 32, 48),
                checks
        );
    }

    public record GeneralSettings(boolean opBypass, int decayIntervalTicks, int cleanupMinutes, double tpsFloor, double lagCompensationFactor) {}
    public record AlertSettings(boolean enabled, int cooldownSeconds, boolean verbose, String permission) {}
    public record AnnouncementSettings(boolean enabled, double confidenceThreshold, int cooldownSeconds, boolean broadcastOnPunish, String discordWebhookUrl, String title) {}
    public record LoggingSettings(boolean enabled, boolean logViolations, boolean logPunishments, boolean logAnnouncements, boolean logDebugTraces) {}
    public record PunishmentSettings(boolean enabled, PunishmentAction action, double confidenceThreshold, String kickCommandTemplate, String tempBanCommandTemplate, String discordWebhookUrl, int globalVlThreshold) {}
    public record TrackingSettings(int movementHistory, int rotationHistory, int velocityHistory, int clickHistory, int combatHistory) {}

    public enum PunishmentAction {
        KICK
    }
}