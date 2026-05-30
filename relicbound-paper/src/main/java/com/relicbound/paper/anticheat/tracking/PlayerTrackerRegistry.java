package com.relicbound.paper.anticheat.tracking;

import com.relicbound.paper.anticheat.config.AnticheatConfig;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerTrackerRegistry {
    private final Map<UUID, PlayerTracker> trackers = new ConcurrentHashMap<>();
    private final AnticheatConfig config;

    public PlayerTrackerRegistry(AnticheatConfig config) {
        this.config = config;
    }

    public PlayerTracker getOrCreate(Player player) {
        return this.trackers.computeIfAbsent(player.getUniqueId(), key -> new PlayerTracker(key, this.config));
    }

    public PlayerTracker get(UUID playerId) {
        return this.trackers.get(playerId);
    }

    public void remove(UUID playerId) {
        this.trackers.remove(playerId);
    }

    public Collection<PlayerTracker> all() {
        return this.trackers.values();
    }
}