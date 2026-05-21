package com.relicbound.core.catalog;

import com.relicbound.core.model.RelicFamily;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;

import java.util.List;
import java.util.Optional;

public interface SpellCatalog {
    Optional<SpellDefinition> findById(String spellId);

    List<SpellDefinition> allSpells();

    List<SpellDefinition> unlockableSpells(RelicFamily family, RelicTier tier);
}
