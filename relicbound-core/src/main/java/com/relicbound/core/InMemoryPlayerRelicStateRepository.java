package com.relicbound.core;

import com.relicbound.core.model.PlayerRelicState;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPlayerRelicStateRepository implements PlayerRelicStateRepository {
    private final Map<String, PlayerRelicState> states = new ConcurrentHashMap<>();

    @Override
    public Optional<PlayerRelicState> findByPlayerId(String playerId) {
        return Optional.ofNullable(this.states.get(playerId));
    }

    @Override
    public PlayerRelicState save(PlayerRelicState playerRelicState) {
        this.states.put(playerRelicState.playerId(), playerRelicState);
        return playerRelicState;
    }

    @Override
    public Collection<PlayerRelicState> findAll() {
        return this.states.values();
    }
}
