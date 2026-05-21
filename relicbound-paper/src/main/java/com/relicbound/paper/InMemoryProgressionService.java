package com.relicbound.paper;

import com.relicbound.core.progression.ProgressionService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryProgressionService implements ProgressionService {
    private final Map<String, Map<String, Integer>> essenceByPlayer = new ConcurrentHashMap<>();

    @Override
    public int essenceForNextTier(String playerId, String relicId) {
        return 100;
    }

    @Override
    public void grantEssence(String playerId, String essenceType, int amount) {
        essenceByPlayer.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .merge(essenceType, amount, Integer::sum);
    }
}
