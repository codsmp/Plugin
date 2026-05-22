package com.relicbound.core;

import com.relicbound.core.catalog.RelicCatalog;
import com.relicbound.core.model.PlatformCapability;
import com.relicbound.core.model.RelicDefinition;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;
import com.relicbound.core.progression.RelicProgressionService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class DefaultRelicboundCore implements RelicboundCore {
    private CoreContext context;

    @Override
    public void initialize(CoreContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public void shutdown() {
        this.context = null;
    }

    @Override
    public Optional<RelicDefinition> registryLookup(String relicId) {
        if (this.context == null) {
            return Optional.empty();
        }
        return this.context.relicCatalog().findById(relicId);
    }

    @Override
    public List<RelicDefinition> allRelics() {
        if (this.context == null) {
            return List.of();
        }
        return this.context.relicCatalog().allRelics();
    }

    @Override
    public Optional<SpellDefinition> findSpell(String spellId) {
        if (this.context == null) {
            return Optional.empty();
        }
        return this.context.spellCatalog().findById(spellId);
    }

    @Override
    public List<SpellDefinition> allSpells() {
        if (this.context == null) {
            return List.of();
        }
        return this.context.spellCatalog().allSpells();
    }

    @Override
    public Optional<PlayerRelicState> findPlayerState(String playerId) {
        if (this.context == null) {
            return Optional.empty();
        }
        return this.context.playerRelicStateRepository().findByPlayerId(playerId);
    }

    @Override
    public PlayerRelicState createStartingState(String playerId, long seed) {
        if (this.context == null) {
            throw new IllegalStateException("Core is not initialized");
        }
        return this.context.relicCatalog().createStartingState(playerId, seed);
    }

    @Override
    public PlayerRelicState getOrCreateStartingState(String playerId, long seed) {
        return this.findPlayerState(playerId).orElseGet(() -> {
            PlayerRelicState startingState = this.createStartingState(playerId, seed);
            return this.savePlayerState(startingState);
        });
    }

    @Override
    public PlayerRelicState savePlayerState(PlayerRelicState playerRelicState) {
        if (this.context == null) {
            throw new IllegalStateException("Core is not initialized");
        }
        return this.context.playerRelicStateRepository().save(playerRelicState);
    }

    @Override
    public PlayerRelicState learnSpell(String playerId, String spellId) {
        PlayerRelicState state = this.findPlayerState(playerId)
                .orElseThrow(() -> new IllegalStateException("No relic state found for player"));
        if (!this.context.spellCatalog().findById(spellId).isPresent()) {
            throw new IllegalArgumentException("Unknown spell: " + spellId);
        }
        if (state.unlockedAbilities().contains(spellId)) {
            return state;
        }
        Set<String> unlocked = new LinkedHashSet<>(state.unlockedAbilities());
        unlocked.add(spellId);
        return this.savePlayerState(new PlayerRelicState(
                state.playerId(),
                state.relicId(),
                state.tier(),
                state.currentEssence(),
                state.essenceByType(),
                List.copyOf(unlocked)
        ));
    }

    @Override
    public PlayerRelicState grantEssence(String playerId, String essenceType, int amount) {
        PlayerRelicState state = this.getOrCreateStartingState(playerId, playerId.hashCode());
        PlayerRelicState updated = this.context.relicProgressionService().grantEssence(state, essenceType, amount);
        PlayerRelicState current = this.applyUnlockedSpells(updated);
        this.savePlayerState(current);
        while (current.tier() != com.relicbound.core.model.RelicTier.ASCENSION
                && current.currentEssence() >= this.context.relicProgressionService().essenceRequiredForNextTier(current)) {
            current = this.context.relicProgressionService().upgradeTier(current);
            current = this.applyUnlockedSpells(current);
            this.savePlayerState(current);
        }
        return current;
    }

    @Override
    public PlayerRelicState upgradeTier(String playerId) {
        PlayerRelicState state = this.findPlayerState(playerId)
                .orElseThrow(() -> new IllegalStateException("No relic state found for player"));
        PlayerRelicState updated = this.context.relicProgressionService().upgradeTier(state);
        if (updated == state) {
            throw new IllegalStateException("Not enough essence to upgrade relic tier");
        }
        return this.savePlayerState(this.applyUnlockedSpells(updated));
    }

    @Override
    public boolean supports(PlatformCapability capability) {
        return this.context != null && this.context.capabilities().supports(capability);
    }

    @Override
    public Optional<com.relicbound.core.model.PlayerManaState> getPlayerManaState(String playerId) {
        if (this.context == null) {
            return Optional.empty();
        }
        return this.context.playerManaStateRepository().findByPlayerId(playerId);
    }

    @Override
    public com.relicbound.core.model.PlayerManaState getOrCreatePlayerManaState(String playerId, com.relicbound.core.model.PlayerArchetype archetype) {
        if (this.context == null) {
            throw new IllegalStateException("Core is not initialized");
        }
        return this.getPlayerManaState(playerId)
            .orElseGet(() -> this.savePlayerManaState(this.context.manaService().initializePlayerMana(playerId, archetype)));
    }

    @Override
    public com.relicbound.core.model.PlayerManaState savePlayerManaState(com.relicbound.core.model.PlayerManaState manaState) {
        if (this.context == null) {
            throw new IllegalStateException("Core is not initialized");
        }
        return this.context.playerManaStateRepository().save(manaState);
    }

    @Override
    public com.relicbound.core.model.PlayerManaState drainMana(com.relicbound.core.model.PlayerManaState manaState, int amount) {
        if (this.context == null) {
            return manaState;
        }
        return this.context.manaService().drainMana(manaState, amount);
    }

    @Override
    public com.relicbound.core.model.PlayerManaState updateManaRegen(com.relicbound.core.model.PlayerManaState manaState, long currentTimeMillis) {
        if (this.context == null) {
            return manaState;
        }
        return this.context.manaService().updateManaRegen(manaState, currentTimeMillis);
    }

    @Override
    public com.relicbound.core.model.PlayerManaState equipSpell(String playerId, String spellId) {
        if (this.context == null) {
            throw new IllegalStateException("Core is not initialized");
        }
        com.relicbound.core.model.PlayerManaState state = this.getPlayerManaState(playerId)
                .orElseThrow(() -> new IllegalStateException("No mana state found for player"));
        com.relicbound.core.model.PlayerManaState updated = this.context.manaService().equipSpell(state, spellId);
        return this.savePlayerManaState(updated);
    }

    private PlayerRelicState applyUnlockedSpells(PlayerRelicState state) {
        if (this.context == null) {
            return state;
        }
        RelicDefinition relicDefinition = this.context.relicCatalog().findById(state.relicId()).orElse(null);
        if (relicDefinition == null) {
            return state;
        }
        Set<String> unlocked = new LinkedHashSet<>(state.unlockedAbilities());
        this.context.spellCatalog().unlockableSpells(relicDefinition.family(), state.tier()).stream()
                .map(SpellDefinition::id)
                .forEach(unlocked::add);
        return new PlayerRelicState(
                state.playerId(),
                state.relicId(),
                state.tier(),
                state.currentEssence(),
                state.essenceByType(),
                List.copyOf(unlocked)
        );
    }
}
