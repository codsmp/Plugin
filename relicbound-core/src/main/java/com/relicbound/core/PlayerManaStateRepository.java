package com.relicbound.core;

import com.relicbound.core.model.PlayerManaState;

import java.util.Optional;

public interface PlayerManaStateRepository {
    Optional<PlayerManaState> findByPlayerId(String playerId);

    PlayerManaState save(PlayerManaState playerManaState);
}
