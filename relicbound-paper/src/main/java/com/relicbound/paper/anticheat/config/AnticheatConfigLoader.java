package com.relicbound.paper.anticheat.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnticheatConfigLoader {
    private final JavaPlugin plugin;
    private final File file;

    public AnticheatConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "anticheat.yml");
    }

    public AnticheatConfig load() {
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            this.saveDefaults(AnticheatConfig.defaults());
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);
        AnticheatConfig defaults = AnticheatConfig.defaults();
        Map<String, CheckSettings> checks = new LinkedHashMap<>(defaults.checks());
        ConfigurationSection checksSection = yaml.getConfigurationSection("anticheat.checks");
        if (checksSection != null) {
            for (String key : checksSection.getKeys(false)) {
                ConfigurationSection section = checksSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                checks.put(key, this.loadCheck(section, defaults.check(key)));
            }
        }

        return new AnticheatConfig(
                yaml.getBoolean("anticheat.enabled", defaults.enabled()),
                new AnticheatConfig.GeneralSettings(
                        yaml.getBoolean("anticheat.general.op-bypass", defaults.general().opBypass()),
                        yaml.getInt("anticheat.general.decay-interval-ticks", defaults.general().decayIntervalTicks()),
                        yaml.getInt("anticheat.general.cleanup-minutes", defaults.general().cleanupMinutes()),
                        yaml.getDouble("anticheat.general.tps-floor", defaults.general().tpsFloor()),
                        yaml.getDouble("anticheat.general.lag-compensation-factor", defaults.general().lagCompensationFactor())
                ),
                new AnticheatConfig.AlertSettings(
                        yaml.getBoolean("anticheat.alerts.enabled", defaults.alerts().enabled()),
                        yaml.getInt("anticheat.alerts.cooldown-seconds", defaults.alerts().cooldownSeconds()),
                        yaml.getBoolean("anticheat.alerts.verbose", defaults.alerts().verbose()),
                        yaml.getString("anticheat.alerts.permission", defaults.alerts().permission())
                ),
                new AnticheatConfig.AnnouncementSettings(
                        yaml.getBoolean("anticheat.announcements.enabled", defaults.announcements().enabled()),
                        yaml.getDouble("anticheat.announcements.confidence-threshold", defaults.announcements().confidenceThreshold()),
                        yaml.getInt("anticheat.announcements.cooldown-seconds", defaults.announcements().cooldownSeconds()),
                        yaml.getBoolean("anticheat.announcements.broadcast-on-punish", defaults.announcements().broadcastOnPunish()),
                        yaml.getString("anticheat.announcements.discord-webhook-url", defaults.announcements().discordWebhookUrl()),
                        yaml.getString("anticheat.announcements.title", defaults.announcements().title())
                ),
                new AnticheatConfig.LoggingSettings(
                        yaml.getBoolean("anticheat.logging.enabled", defaults.logging().enabled()),
                        yaml.getBoolean("anticheat.logging.log-violations", defaults.logging().logViolations()),
                        yaml.getBoolean("anticheat.logging.log-punishments", defaults.logging().logPunishments()),
                        yaml.getBoolean("anticheat.logging.log-announcements", defaults.logging().logAnnouncements()),
                        yaml.getBoolean("anticheat.logging.log-debug-traces", defaults.logging().logDebugTraces())
                ),
                new AnticheatConfig.PunishmentSettings(
                        yaml.getBoolean("anticheat.punishments.enabled", defaults.punishments().enabled()),
                        this.loadPunishmentAction(yaml.getString("anticheat.punishments.action", defaults.punishments().action().name())),
                        yaml.getDouble("anticheat.punishments.confidence-threshold", defaults.punishments().confidenceThreshold()),
                        yaml.getString("anticheat.punishments.kick-command-template", defaults.punishments().kickCommandTemplate()),
                        yaml.getString("anticheat.punishments.temp-ban-command-template", defaults.punishments().tempBanCommandTemplate()),
                        yaml.getString("anticheat.punishments.discord-webhook-url", defaults.punishments().discordWebhookUrl()),
                        yaml.getInt("anticheat.punishments.global-vl-threshold", defaults.punishments().globalVlThreshold())
                ),
                new AnticheatConfig.TrackingSettings(
                        yaml.getInt("anticheat.tracking.movement-history", defaults.tracking().movementHistory()),
                        yaml.getInt("anticheat.tracking.rotation-history", defaults.tracking().rotationHistory()),
                        yaml.getInt("anticheat.tracking.velocity-history", defaults.tracking().velocityHistory()),
                        yaml.getInt("anticheat.tracking.click-history", defaults.tracking().clickHistory()),
                        yaml.getInt("anticheat.tracking.combat-history", defaults.tracking().combatHistory())
                ),
                checks
        );
    }

    public void saveDefaults(AnticheatConfig config) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("anticheat.enabled", config.enabled());
        yaml.set("anticheat.general.op-bypass", config.general().opBypass());
        yaml.set("anticheat.general.decay-interval-ticks", config.general().decayIntervalTicks());
        yaml.set("anticheat.general.cleanup-minutes", config.general().cleanupMinutes());
        yaml.set("anticheat.general.tps-floor", config.general().tpsFloor());
        yaml.set("anticheat.general.lag-compensation-factor", config.general().lagCompensationFactor());
        yaml.set("anticheat.alerts.enabled", config.alerts().enabled());
        yaml.set("anticheat.alerts.cooldown-seconds", config.alerts().cooldownSeconds());
        yaml.set("anticheat.alerts.verbose", config.alerts().verbose());
        yaml.set("anticheat.alerts.permission", config.alerts().permission());
        yaml.set("anticheat.announcements.enabled", config.announcements().enabled());
        yaml.set("anticheat.announcements.confidence-threshold", config.announcements().confidenceThreshold());
        yaml.set("anticheat.announcements.cooldown-seconds", config.announcements().cooldownSeconds());
        yaml.set("anticheat.announcements.broadcast-on-punish", config.announcements().broadcastOnPunish());
        yaml.set("anticheat.announcements.discord-webhook-url", config.announcements().discordWebhookUrl());
        yaml.set("anticheat.announcements.title", config.announcements().title());
        yaml.set("anticheat.logging.enabled", config.logging().enabled());
        yaml.set("anticheat.logging.log-violations", config.logging().logViolations());
        yaml.set("anticheat.logging.log-punishments", config.logging().logPunishments());
        yaml.set("anticheat.logging.log-announcements", config.logging().logAnnouncements());
        yaml.set("anticheat.logging.log-debug-traces", config.logging().logDebugTraces());
        yaml.set("anticheat.punishments.enabled", config.punishments().enabled());
        yaml.set("anticheat.punishments.action", config.punishments().action().name());
        yaml.set("anticheat.punishments.confidence-threshold", config.punishments().confidenceThreshold());
        yaml.set("anticheat.punishments.kick-command-template", config.punishments().kickCommandTemplate());
        yaml.set("anticheat.punishments.temp-ban-command-template", config.punishments().tempBanCommandTemplate());
        yaml.set("anticheat.punishments.discord-webhook-url", config.punishments().discordWebhookUrl());
        yaml.set("anticheat.punishments.global-vl-threshold", config.punishments().globalVlThreshold());
        yaml.set("anticheat.tracking.movement-history", config.tracking().movementHistory());
        yaml.set("anticheat.tracking.rotation-history", config.tracking().rotationHistory());
        yaml.set("anticheat.tracking.velocity-history", config.tracking().velocityHistory());
        yaml.set("anticheat.tracking.click-history", config.tracking().clickHistory());
        yaml.set("anticheat.tracking.combat-history", config.tracking().combatHistory());

        for (Map.Entry<String, CheckSettings> entry : config.checks().entrySet()) {
            String path = "anticheat.checks." + entry.getKey();
            CheckSettings settings = entry.getValue();
            yaml.set(path + ".enabled", settings.enabled());
            yaml.set(path + ".threshold", settings.threshold());
            yaml.set(path + ".punish-threshold", settings.punishThreshold());
            yaml.set(path + ".decay-per-second", settings.decayPerSecond());
            yaml.set(path + ".confidence-weight", settings.confidenceWeight());
            yaml.set(path + ".alert-cooldown-seconds", settings.alertCooldownSeconds());
            yaml.set(path + ".buffer", settings.buffer());
            yaml.set(path + ".announce-on-punish", settings.announceOnPunish());
        }

        try {
            yaml.save(this.file);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to save anticheat.yml: " + exception.getMessage());
        }
    }

    private CheckSettings loadCheck(ConfigurationSection section, CheckSettings fallback) {
        return new CheckSettings(
                section.getBoolean("enabled", fallback.enabled()),
                section.getDouble("threshold", fallback.threshold()),
                section.getDouble("punish-threshold", fallback.punishThreshold()),
                section.getDouble("decay-per-second", fallback.decayPerSecond()),
                section.getDouble("confidence-weight", fallback.confidenceWeight()),
                section.getInt("alert-cooldown-seconds", fallback.alertCooldownSeconds()),
                section.getDouble("buffer", fallback.buffer()),
                section.getBoolean("announce-on-punish", fallback.announceOnPunish())
        );
    }

    private AnticheatConfig.PunishmentAction loadPunishmentAction(String value) {
        try {
            return AnticheatConfig.PunishmentAction.valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return AnticheatConfig.PunishmentAction.KICK;
        }
    }
}