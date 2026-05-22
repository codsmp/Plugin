package com.relicbound.paper;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SpellSelectionSession {
    private static final Set<String> PENDING_REWARD_SELECTIONS = ConcurrentHashMap.newKeySet();

    private SpellSelectionSession() {
    }

    public static void beginRewardSelection(String playerId) {
        PENDING_REWARD_SELECTIONS.add(playerId);
    }

    public static void completeRewardSelection(String playerId) {
        PENDING_REWARD_SELECTIONS.remove(playerId);
    }

    public static boolean isRewardSelectionPending(String playerId) {
        return PENDING_REWARD_SELECTIONS.contains(playerId);
    }
}