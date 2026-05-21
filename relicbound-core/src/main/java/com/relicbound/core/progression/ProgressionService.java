package com.relicbound.core.progression;

public interface ProgressionService {
    int essenceForNextTier(String playerId, String relicId);

    void grantEssence(String playerId, String essenceType, int amount);
}
