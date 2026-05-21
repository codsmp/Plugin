package com.relicbound.core.platform;

import com.relicbound.core.PlayerRelicStateRepository;
import com.relicbound.core.PlayerManaStateRepository;
import com.relicbound.core.catalog.RelicCatalog;
import com.relicbound.core.catalog.SpellCatalog;
import com.relicbound.core.model.PlatformCapabilities;
import com.relicbound.core.progression.ProgressionService;
import com.relicbound.core.progression.RelicProgressionService;
import com.relicbound.core.progression.ManaService;

public interface PlatformAdapter {
    String platformId();

    LoaderFamily loaderFamily();

    PlatformCapabilities capabilities();

    ProgressionService progressionService();

    RelicProgressionService relicProgressionService();

    RelicCatalog relicCatalog();

    SpellCatalog spellCatalog();

    ManaService manaService();

    PlayerRelicStateRepository playerRelicStateRepository();

    PlayerManaStateRepository playerManaStateRepository();
}
