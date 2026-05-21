package com.relicbound.core;

import com.relicbound.core.model.PlayerRelicState;

import java.util.Collection;
import java.util.Optional;

public interface PlayerRelicStateRepository {
    Optional<PlayerRelicState> findByPlayerId(String playerId);

    PlayerRelicState save(PlayerRelicState playerRelicState);

    Collection<PlayerRelicState> findAll();
}
