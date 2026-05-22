package com.relicbound.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerTrustStore {
    private final JavaPlugin plugin;
    private final File storageFile;
    private final Map<String, Set<String>> trustedByOwner = new HashMap<>();

    public PlayerTrustStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "trusted-players.yml");
        this.load();
    }

    public synchronized boolean toggleTrust(String ownerId, String trustedPlayerId) {
        Set<String> trusted = this.trustedByOwner.computeIfAbsent(ownerId, ignored -> new HashSet<>());
        boolean added;
        if (trusted.contains(trustedPlayerId)) {
            trusted.remove(trustedPlayerId);
            added = false;
        } else {
            trusted.add(trustedPlayerId);
            added = true;
        }
        this.persist();
        return added;
    }

    public synchronized boolean isTrustedEitherWay(String firstPlayerId, String secondPlayerId) {
        return this.isTrusted(firstPlayerId, secondPlayerId) || this.isTrusted(secondPlayerId, firstPlayerId);
    }

    public synchronized boolean isTrusted(String ownerId, String trustedPlayerId) {
        return this.trustedByOwner.getOrDefault(ownerId, Set.of()).contains(trustedPlayerId);
    }

    private void load() {
        if (!this.storageFile.exists()) {
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.storageFile);
        if (configuration.getConfigurationSection("trusted") == null) {
            return;
        }

        for (String ownerId : configuration.getConfigurationSection("trusted").getKeys(false)) {
            this.trustedByOwner.put(ownerId, new HashSet<>(configuration.getStringList("trusted." + ownerId + ".players")));
        }
    }

    private void persist() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (Map.Entry<String, Set<String>> entry : this.trustedByOwner.entrySet()) {
            configuration.set("trusted." + entry.getKey() + ".players", entry.getValue().stream().toList());
        }

        try {
            this.storageFile.getParentFile().mkdirs();
            configuration.save(this.storageFile);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to save trust data: " + exception.getMessage());
        }
    }
}