package com.relicbound.core.progression;

import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerArchetype;

import java.util.List;

public interface ManaService {
    PlayerManaState initializePlayerMana(String playerId, PlayerArchetype archetype);

    PlayerManaState updateManaRegen(PlayerManaState state, long currentTimeMillis);

    PlayerManaState drainMana(PlayerManaState state, int amount);

    PlayerManaState restoreMana(PlayerManaState state, int amount);

    PlayerManaState equipSpell(PlayerManaState state, String spellId);

    PlayerManaState unequipSpell(PlayerManaState state, String spellId);
}
