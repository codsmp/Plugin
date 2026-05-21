package com.relicbound.core.model;

import java.util.List;

public record RelicDefinition(
        String id,
        String displayName,
        RelicFamily family,
        RelicRarity rarity,
        List<String> passiveIds,
        List<String> activeIds,
        List<String> loreTags
) {
}
