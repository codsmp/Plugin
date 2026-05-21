package com.relicbound.core.catalog;

import com.relicbound.core.model.RelicDefinition;
import com.relicbound.core.model.PlayerRelicState;

import java.util.List;
import java.util.Optional;

public interface RelicCatalog {
    Optional<RelicDefinition> findById(String relicId);

    List<RelicDefinition> allRelics();

    PlayerRelicState createStartingState(String playerId, long seed);
}
