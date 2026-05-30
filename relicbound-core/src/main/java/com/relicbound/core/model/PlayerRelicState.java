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
    boolean pendingRewardSelection,
    int pendingRewardSelections
) {
    public PlayerRelicState withPendingRewardSelection(boolean pendingRewardSelection) {
        return new PlayerRelicState(
                this.playerId,
                this.relicId,
                this.tier,
                this.currentEssence,
                this.essenceByType,
                this.unlockedAbilities,
                pendingRewardSelection,
                pendingRewardSelection ? Math.max(this.pendingRewardSelections, 1) : 0
        );
    }

    public PlayerRelicState withPendingRewardSelections(int pendingRewardSelections) {
        int normalized = Math.max(0, pendingRewardSelections);
        return new PlayerRelicState(
                this.playerId,
                this.relicId,
                this.tier,
                this.currentEssence,
                this.essenceByType,
                this.unlockedAbilities,
                normalized > 0,
                normalized
        );
    }
}
