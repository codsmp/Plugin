package com.relicbound.paper;

import com.relicbound.core.PlayerManaStateRepository;
import com.relicbound.core.model.PlayerManaState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;

public final class YamlPlayerManaStateRepository implements PlayerManaStateRepository {
    private final JavaPlugin plugin;

    public YamlPlayerManaStateRepository(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<PlayerManaState> findByPlayerId(String playerId) {
        File file = this.getPlayerFile(playerId);
        if (!file.exists()) {
            return Optional.empty();
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            return Optional.of(this.loadFromConfig(playerId, config));
        } catch (Exception exception) {
            this.plugin.getLogger().severe("Failed to load player mana state: " + exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public PlayerManaState save(PlayerManaState playerManaState) {
        File file = this.getPlayerFile(playerManaState.playerId());
        try {
            file.getParentFile().mkdirs();
            YamlConfiguration config = new YamlConfiguration();
            config.set("playerId", playerManaState.playerId());
            config.set("archetype", playerManaState.archetype().name());
            config.set("currentMana", playerManaState.currentMana());
            config.set("maxMana", playerManaState.maxMana());
            config.set("equippedSpellIds", playerManaState.equippedSpellIds());
            config.set("availableScrollIds", playerManaState.availableScrollIds());
            config.set("lastManaRegenTime", playerManaState.lastManaRegenTime());
            config.save(file);
            return playerManaState;
        } catch (Exception exception) {
            this.plugin.getLogger().severe("Failed to save player mana state: " + exception.getMessage());
            return playerManaState;
        }
    }

    private PlayerManaState loadFromConfig(String playerId, YamlConfiguration config) {
        return new PlayerManaState(
                playerId,
                com.relicbound.core.model.PlayerArchetype.valueOf(config.getString("archetype", "WAND")),
                config.getInt("currentMana", 100),
                config.getInt("maxMana", 100),
                config.getStringList("equippedSpellIds"),
                config.getStringList("availableScrollIds"),
                config.getLong("lastManaRegenTime", System.currentTimeMillis())
        );
    }

    private File getPlayerFile(String playerId) {
        return new File(this.plugin.getDataFolder(), "data" + File.separator + "mana" + File.separator + playerId + ".yml");
    }
}
