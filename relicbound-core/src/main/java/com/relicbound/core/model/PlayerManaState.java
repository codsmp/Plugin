package com.relicbound.core.model;

import java.util.List;

public record PlayerManaState(
        String playerId,
        PlayerArchetype archetype,
        int currentMana,
        int maxMana,
        List<String> equippedSpellIds,
        List<String> availableScrollIds,
        long lastManaRegenTime
) {
    public int getManaRegenPerSecond() {
        return this.archetype == PlayerArchetype.WAND ? 5 : 3;
    }
}
