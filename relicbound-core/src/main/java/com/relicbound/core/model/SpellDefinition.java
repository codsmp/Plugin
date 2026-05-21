package com.relicbound.core.model;

import java.util.List;

public record SpellDefinition(
        String id,
        String displayName,
        String modelKey,
        MaterialBinding icon,
        RelicTier requiredTier,
        SpellEffectType effectType,
        List<RelicFamily> affinities,
        int cooldownTicks,
        double power,
        double range,
        int durationTicks,
        String description,
        int manaCost,
        int manaPerSecond
) {
    public record MaterialBinding(String material, Integer customModelData) {
    }
}
