package com.relicbound.core;

import com.relicbound.core.catalog.RelicCatalog;
import com.relicbound.core.model.PlatformCapabilities;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.catalog.SpellCatalog;
import com.relicbound.core.progression.RelicProgressionService;
import com.relicbound.core.progression.ProgressionService;
import com.relicbound.core.progression.ManaService;

public record CoreContext(
        PlatformCapabilities capabilities,
        ProgressionService progressionService,
        RelicCatalog relicCatalog,
        SpellCatalog spellCatalog,
        RelicProgressionService relicProgressionService,
        ManaService manaService,
        PlayerRelicStateRepository playerRelicStateRepository,
        PlayerManaStateRepository playerManaStateRepository
) {
}
