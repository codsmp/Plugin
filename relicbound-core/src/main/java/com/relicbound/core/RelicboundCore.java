package com.relicbound.core;

import com.relicbound.core.model.PlatformCapability;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.RelicDefinition;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.SpellDefinition;

import java.util.List;
import java.util.Optional;

public interface RelicboundCore {
    void initialize(CoreContext context);

    void shutdown();

    Optional<RelicDefinition> registryLookup(String relicId);

    List<RelicDefinition> allRelics();

    Optional<SpellDefinition> findSpell(String spellId);

    List<SpellDefinition> allSpells();

    Optional<PlayerRelicState> findPlayerState(String playerId);

    PlayerRelicState createStartingState(String playerId, long seed);

    PlayerRelicState getOrCreateStartingState(String playerId, long seed);

    PlayerRelicState savePlayerState(PlayerRelicState playerRelicState);

    PlayerRelicState learnSpell(String playerId, String spellId);

    PlayerRelicState grantEssence(String playerId, String essenceType, int amount);

    PlayerRelicState upgradeTier(String playerId);

    Optional<PlayerManaState> getPlayerManaState(String playerId);

    PlayerManaState getOrCreatePlayerManaState(String playerId, PlayerArchetype archetype);

    PlayerManaState savePlayerManaState(PlayerManaState manaState);

    PlayerManaState drainMana(PlayerManaState manaState, int amount);

    PlayerManaState updateManaRegen(PlayerManaState manaState, long currentTimeMillis);

    PlayerManaState equipSpell(String playerId, String spellId);

    boolean supports(PlatformCapability capability);
}
