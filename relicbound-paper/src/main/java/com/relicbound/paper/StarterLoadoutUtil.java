package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class StarterLoadoutUtil {
    private static final int STARTER_SPELL_COUNT = 2;

    private StarterLoadoutUtil() {
    }

    public static List<SpellDefinition> randomStarterSpells(RelicboundCore core) {
        List<SpellDefinition> starterPool = core.allSpells().stream()
                .filter(spell -> spell.requiredTier() == RelicTier.TIER_1)
                .toList();
        if (starterPool.isEmpty()) {
            return List.of();
        }

        List<SpellDefinition> shuffled = new ArrayList<>(starterPool);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int swapIndex = random.nextInt(i + 1);
            SpellDefinition current = shuffled.get(i);
            shuffled.set(i, shuffled.get(swapIndex));
            shuffled.set(swapIndex, current);
        }

        return List.copyOf(shuffled.subList(0, Math.min(STARTER_SPELL_COUNT, shuffled.size())));
    }

    public static void grantRandomStarterLoadout(RelicboundCore core, String playerId) {
        com.relicbound.core.model.PlayerRelicState relicState = core.findPlayerState(playerId).orElse(null);
        if (relicState != null) {
            boolean alreadyHasUnlockedSpells = relicState.unlockedAbilities().stream().anyMatch(id -> core.findSpell(id).isPresent());
            if (alreadyHasUnlockedSpells) {
                return;
            }
        }

        for (SpellDefinition spell : randomStarterSpells(core)) {
            try {
                core.learnSpell(playerId, spell.id());
                core.equipSpell(playerId, spell.id());
            } catch (Exception ignored) {
                // Starter loadout should be best-effort and never block selection.
            }
        }
    }
}