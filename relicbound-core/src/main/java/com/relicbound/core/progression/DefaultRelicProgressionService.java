package com.relicbound.core.progression;

import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicTier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultRelicProgressionService implements RelicProgressionService {
    private static final List<Integer> TIER_THRESHOLDS = List.of(0, 100, 250, 500, 900, 1500);

    @Override
    public PlayerRelicState grantEssence(PlayerRelicState state, String essenceType, int amount) {
        Map<String, Integer> essenceByType = new HashMap<>(state.essenceByType());
        essenceByType.merge(essenceType, amount, Integer::sum);
        return new PlayerRelicState(
                state.playerId(),
                state.relicId(),
                state.tier(),
                Math.max(0, state.currentEssence() + amount),
                Map.copyOf(essenceByType),
                state.unlockedAbilities()
        );
    }

    @Override
    public PlayerRelicState upgradeTier(PlayerRelicState state) {
        int requiredEssence = essenceRequiredForNextTier(state);
        if (state.currentEssence() < requiredEssence) {
            return state;
        }
        RelicTier nextTier = nextTier(state.tier());
        if (nextTier == state.tier()) {
            return state;
        }
        return new PlayerRelicState(
                state.playerId(),
                state.relicId(),
                nextTier,
                Math.max(0, state.currentEssence() - requiredEssence),
                state.essenceByType(),
                state.unlockedAbilities()
        );
    }

    @Override
    public int essenceRequiredForNextTier(PlayerRelicState state) {
        int currentIndex = tierIndex(state.tier());
        if (currentIndex >= TIER_THRESHOLDS.size() - 1) {
            return Integer.MAX_VALUE;
        }
        return TIER_THRESHOLDS.get(currentIndex + 1);
    }

    private RelicTier nextTier(RelicTier tier) {
        return switch (tier) {
            case TIER_1 -> RelicTier.TIER_2;
            case TIER_2 -> RelicTier.TIER_3;
            case TIER_3 -> RelicTier.TIER_4;
            case TIER_4 -> RelicTier.TIER_5;
            case TIER_5 -> RelicTier.ASCENSION;
            case ASCENSION -> RelicTier.ASCENSION;
        };
    }

    private int tierIndex(RelicTier tier) {
        return switch (tier) {
            case TIER_1 -> 0;
            case TIER_2 -> 1;
            case TIER_3 -> 2;
            case TIER_4 -> 3;
            case TIER_5 -> 4;
            case ASCENSION -> 5;
        };
    }
}
