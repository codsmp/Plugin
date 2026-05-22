package com.relicbound.core.model;

import java.util.List;
import java.util.Map;

public record PlayerRelicState(
        String playerId,
        String relicId,
        RelicTier tier,
        int currentEssence,
        Map<String, Integer> essenceByType,
        List<String> unlockedAbilities,
        boolean pendingRewardSelection
) {
    public PlayerRelicState withPendingRewardSelection(boolean pendingRewardSelection) {
        return new PlayerRelicState(
                this.playerId,
                this.relicId,
                this.tier,
                this.currentEssence,
                this.essenceByType,
                this.unlockedAbilities,
                pendingRewardSelection
        );
    }
}
