package com.relicbound.paper.anticheat;

import com.relicbound.paper.anticheat.config.AnticheatConfig;
import com.relicbound.paper.anticheat.config.AnticheatConfigLoader;
import com.relicbound.paper.anticheat.confidence.ConfidenceEngine;
import com.relicbound.paper.anticheat.tracking.PlayerTrackerRegistry;
import com.relicbound.paper.anticheat.violations.ViolationEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnticheatService {
    private final JavaPlugin plugin;
    private final AnticheatConfigLoader loader;
    private AnticheatConfig config;
    private final PlayerTrackerRegistry registry;
    private final ViolationEngine violationEngine;
    private final ConfidenceEngine confidenceEngine;
    private final com.relicbound.paper.anticheat.checks.CheckRegistry checkRegistry;
    private final com.relicbound.paper.anticheat.alerts.AlertService alertService;
    private final com.relicbound.paper.anticheat.announcements.AnnouncementService announcementService;
    private final Map<UUID, Long> punishmentCooldownUntil = new ConcurrentHashMap<>();
    private volatile boolean enabled;

    public AnticheatService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loader = new AnticheatConfigLoader(plugin);
        this.config = this.loader.load();
        this.registry = new PlayerTrackerRegistry(this.config);
        this.violationEngine = new ViolationEngine();
        this.confidenceEngine = new ConfidenceEngine();
        this.enabled = this.config.enabled();
        this.checkRegistry = new com.relicbound.paper.anticheat.checks.CheckRegistry();
        // register core checks
        this.checkRegistry.register(new com.relicbound.paper.anticheat.checks.movement.SpeedCheck());
        this.checkRegistry.register(new com.relicbound.paper.anticheat.checks.movement.FlyCheck());
        this.checkRegistry.register(new com.relicbound.paper.anticheat.checks.combat.ReachCheck());
        this.alertService = new com.relicbound.paper.anticheat.alerts.AlertService(this.config);
        this.announcementService = new com.relicbound.paper.anticheat.announcements.AnnouncementService(this.config);
        // schedule periodic check runner
        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (!this.enabled) return;
            long now = System.nanoTime();
            for (java.util.UUID pid : this.registry.all().stream().map(t -> t.playerId()).toList()) {
                for (var check : this.checkRegistry.all()) {
                    try {
                        check.runForPlayer(pid, now, this);
                    } catch (Throwable ignored) {}
                }
            }
            this.tick(now);
        }, 2L, 2L);
    }

    public void enable() {
        if (this.enabled) return;
        this.enabled = true;
        this.plugin.getLogger().info("WitchSMP Anticheat enabled");
    }

    public void disable() {
        if (!this.enabled) return;
        this.enabled = false;
        this.plugin.getLogger().info("WitchSMP Anticheat disabled");
    }

    public boolean enabled() {
        return this.enabled;
    }

    public PlayerTrackerRegistry registry() {
        return this.registry;
    }

    public ViolationEngine violations() {
        return this.violationEngine;
    }

    public ConfidenceEngine confidence() {
        return this.confidenceEngine;
    }

    public AnticheatConfig config() {
        return this.config;
    }

    public void reload() {
        try {
            this.config = this.loader.load();
            this.plugin.getLogger().info("WitchSMP Anticheat config reloaded");
        } catch (Throwable t) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to reload anticheat config", t);
        }
    }

    public void tick(long nowNanos) {
        if (!this.enabled) return;
        // Decay violations and confidences
        this.violationEngine.decayAll(this.config, nowNanos);
        this.confidenceEngine.decayAll(nowNanos, 1.0D);
        // handle punishments conservatively
        for (var t : this.registry.all()) {
            java.util.UUID pid = t.playerId();
            double totalVl = this.violationEngine.totalVl(pid);
            double conf = this.confidenceEngine.confidence(pid);
            long cooldownUntil = this.punishmentCooldownUntil.getOrDefault(pid, 0L);
            if (nowNanos < cooldownUntil) {
                continue;
            }
            if (this.config.punishments().enabled() && totalVl >= this.config.punishments().globalVlThreshold() && conf >= this.config.punishments().confidenceThreshold()) {
                // perform punishment
                String playerName = "(unknown)";
                var player = org.bukkit.Bukkit.getPlayer(pid);
                if (player != null) playerName = player.getName();
                // Kick only; ban actions are intentionally not executed by this service.
                if (this.config.punishments().action() == AnticheatConfig.PunishmentAction.KICK) {
                    if (player != null) {
                        player.kickPlayer(this.config.punishments().kickCommandTemplate().replace("{player}", playerName));
                    }
                    this.alertService.alertStaff(playerName + " reached VL " + totalVl, "Confidence: " + conf);
                    this.announcementService.announcePunish(playerName, "VL:" + totalVl);
                }
                // reset VL and add a cooldown so the same offense cannot spam repeated kicks
                this.violationEngine.state(pid).all().clear();
                this.confidenceEngine.clear(pid);
                this.punishmentCooldownUntil.put(pid, nowNanos + 60L * 1_000_000_000L);
            }
        }
    }
}