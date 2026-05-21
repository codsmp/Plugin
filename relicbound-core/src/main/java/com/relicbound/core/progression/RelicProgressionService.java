package com.relicbound.core.progression;

import com.relicbound.core.model.PlayerRelicState;

public interface RelicProgressionService {
    PlayerRelicState grantEssence(PlayerRelicState state, String essenceType, int amount);

    PlayerRelicState upgradeTier(PlayerRelicState state);

    int essenceRequiredForNextTier(PlayerRelicState state);
}
