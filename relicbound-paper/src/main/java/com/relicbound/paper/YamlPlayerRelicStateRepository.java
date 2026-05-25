package com.relicbound.paper;

import com.relicbound.core.PlayerRelicStateRepository;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicTier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class YamlPlayerRelicStateRepository implements PlayerRelicStateRepository {
    private final JavaPlugin plugin;
    private final File storageFile;
    private final Map<String, PlayerRelicState> cache = new ConcurrentHashMap<>();

    public YamlPlayerRelicStateRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "player-states.yml");
        this.load();
    }

    @Override
    public Optional<PlayerRelicState> findByPlayerId(String playerId) {
        return Optional.ofNullable(this.cache.get(playerId));
    }

    @Override
    public PlayerRelicState save(PlayerRelicState playerRelicState) {
        this.cache.put(playerRelicState.playerId(), playerRelicState);
        this.persist();
        return playerRelicState;
    }

    @Override
    public Collection<PlayerRelicState> findAll() {
        return List.copyOf(this.cache.values());
    }

    public void clear() {
        this.cache.clear();
        if (this.storageFile.exists() && !this.storageFile.delete()) {
            this.plugin.getLogger().warning("Failed to delete " + this.storageFile.getAbsolutePath());
        }
    }

    private void load() {
        if (!this.storageFile.exists()) {
            this.storageFile.getParentFile().mkdirs();
            return;
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.storageFile);
        for (String playerId : configuration.getKeys(false)) {
            String relicId = configuration.getString(playerId + ".relicId", "");
            String tierName = configuration.getString(playerId + ".tier", RelicTier.TIER_1.name());
            RelicTier tier;
            if ("ASCENSION".equalsIgnoreCase(tierName)) {
                // Legacy value: map old single ASCENSION to the new max ascension tier
                tier = RelicTier.ASCENSION_5;
            } else {
                try {
                    tier = RelicTier.valueOf(tierName);
                } catch (IllegalArgumentException ex) {
                    tier = RelicTier.TIER_1;
                }
            }
            int essence = configuration.getInt(playerId + ".essence", 0);
            Map<String, Integer> essenceByType = new HashMap<>();
            if (configuration.getConfigurationSection(playerId + ".essenceByType") != null) {
                for (String essenceKey : configuration.getConfigurationSection(playerId + ".essenceByType").getKeys(false)) {
                    essenceByType.put(essenceKey, configuration.getInt(playerId + ".essenceByType." + essenceKey));
                }
            }
            List<String> unlockedAbilities = new ArrayList<>(configuration.getStringList(playerId + ".unlockedAbilities"));
            boolean pendingRewardSelection = configuration.getBoolean(playerId + ".pendingRewardSelection", false);
            this.cache.put(playerId, new PlayerRelicState(playerId, relicId, tier, essence, essenceByType, unlockedAbilities, pendingRewardSelection));
        }
    }

    private void persist() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (PlayerRelicState state : this.cache.values()) {
            String path = state.playerId();
            configuration.set(path + ".relicId", state.relicId());
            configuration.set(path + ".tier", state.tier().name());
            configuration.set(path + ".essence", state.currentEssence());
            configuration.set(path + ".essenceByType", state.essenceByType());
            configuration.set(path + ".unlockedAbilities", state.unlockedAbilities());
            configuration.set(path + ".pendingRewardSelection", state.pendingRewardSelection());
        }
        try {
            configuration.save(this.storageFile);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to save player relic state: " + exception.getMessage());
        }
    }
}
