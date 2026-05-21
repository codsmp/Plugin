package com.relicbound.core.progression;

import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerArchetype;

import java.util.ArrayList;
import java.util.List;

public final class DefaultManaService implements ManaService {
    private static final int BASE_MAX_MANA = 100;
    private static final int MAX_EQUIPPED_SPELLS = 2;

    @Override
    public PlayerManaState initializePlayerMana(String playerId, PlayerArchetype archetype) {
        return new PlayerManaState(
                playerId,
                archetype,
                BASE_MAX_MANA,
                BASE_MAX_MANA,
                new ArrayList<>(),
                new ArrayList<>(),
                System.currentTimeMillis()
        );
    }

    @Override
    public PlayerManaState updateManaRegen(PlayerManaState state, long currentTimeMillis) {
        long timeSinceLastRegen = currentTimeMillis - state.lastManaRegenTime();
        if (timeSinceLastRegen < 1000) {
            return state;
        }

        int secondsElapsed = (int) (timeSinceLastRegen / 1000L);
        int regenPerSecond = state.getManaRegenPerSecond();
        int manaToRestore = secondsElapsed * regenPerSecond;

        int newMana = Math.min(state.maxMana(), state.currentMana() + manaToRestore);
        return new PlayerManaState(
                state.playerId(),
                state.archetype(),
                newMana,
                state.maxMana(),
                state.equippedSpellIds(),
                state.availableScrollIds(),
                currentTimeMillis
        );
    }

    @Override
    public PlayerManaState drainMana(PlayerManaState state, int amount) {
        int newMana = Math.max(0, state.currentMana() - amount);
        return new PlayerManaState(
                state.playerId(),
                state.archetype(),
                newMana,
                state.maxMana(),
                state.equippedSpellIds(),
                state.availableScrollIds(),
                state.lastManaRegenTime()
        );
    }

    @Override
    public PlayerManaState restoreMana(PlayerManaState state, int amount) {
        int newMana = Math.min(state.maxMana(), state.currentMana() + amount);
        return new PlayerManaState(
                state.playerId(),
                state.archetype(),
                newMana,
                state.maxMana(),
                state.equippedSpellIds(),
                state.availableScrollIds(),
                state.lastManaRegenTime()
        );
    }

    @Override
    public PlayerManaState equipSpell(PlayerManaState state, String spellId) {
        if (state.equippedSpellIds().size() >= MAX_EQUIPPED_SPELLS) {
            return state;
        }
        if (state.equippedSpellIds().contains(spellId)) {
            return state;
        }

        List<String> equipped = new ArrayList<>(state.equippedSpellIds());
        equipped.add(spellId);

        return new PlayerManaState(
                state.playerId(),
                state.archetype(),
                state.currentMana(),
                state.maxMana(),
                equipped,
                state.availableScrollIds(),
                state.lastManaRegenTime()
        );
    }

    @Override
    public PlayerManaState unequipSpell(PlayerManaState state, String spellId) {
        List<String> equipped = new ArrayList<>(state.equippedSpellIds());
        equipped.remove(spellId);

        return new PlayerManaState(
                state.playerId(),
                state.archetype(),
                state.currentMana(),
                state.maxMana(),
                equipped,
                state.availableScrollIds(),
                state.lastManaRegenTime()
        );
    }
}
