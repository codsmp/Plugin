package com.relicbound.paper;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EnchantLimitStore {
    private final JavaPlugin plugin;
    private final File storageFile;
    private final Map<String, Integer> limits = new HashMap<>();

    public EnchantLimitStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "enchant-limits.yml");
        this.load();
    }

    public synchronized void setLimit(Enchantment enchantment, int limit) {
        this.limits.put(this.keyOf(enchantment), limit);
        this.persist();
    }

    public synchronized Optional<Integer> getLimit(Enchantment enchantment) {
        return Optional.ofNullable(this.limits.get(this.keyOf(enchantment)));
    }

    public synchronized Map<String, Integer> getAllLimits() {
        return Collections.unmodifiableMap(new HashMap<>(this.limits));
    }

    public synchronized boolean isAllowed(Enchantment enchantment, int level) {
        // Enchant limits disabled: always allow any enchantment level
        return true;
    }

    public synchronized void clear() {
        this.limits.clear();
        if (this.storageFile.exists() && !this.storageFile.delete()) {
            this.plugin.getLogger().warning("Failed to delete " + this.storageFile.getAbsolutePath());
        }
    }

    public static Optional<Enchantment> resolveEnchantment(String input) {
        String normalized = input.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        try {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(normalized.toLowerCase()));
            if (enchantment != null) {
                return Optional.of(enchantment);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return Optional.empty();
    }

    private void load() {
        if (!this.storageFile.exists()) {
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.storageFile);
        if (configuration.getConfigurationSection("limits") == null) {
            return;
        }

        for (String key : configuration.getConfigurationSection("limits").getKeys(false)) {
            this.limits.put(key.toUpperCase(), configuration.getInt("limits." + key));
        }
    }

    private void persist() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (Map.Entry<String, Integer> entry : this.limits.entrySet()) {
            configuration.set("limits." + entry.getKey(), entry.getValue());
        }

        try {
            this.storageFile.getParentFile().mkdirs();
            configuration.save(this.storageFile);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to save enchant limit data: " + exception.getMessage());
        }
    }

    private String keyOf(Enchantment enchantment) {
        return enchantment.getKey().getKey().toUpperCase();
    }
}