package com.relicbound.core.catalog;

import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicDefinition;
import com.relicbound.core.model.RelicFamily;
import com.relicbound.core.model.RelicRarity;
import com.relicbound.core.model.RelicTier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultRelicCatalog implements RelicCatalog {
    private final Map<String, RelicDefinition> relics = new ConcurrentHashMap<>();

    public DefaultRelicCatalog() {
        register(new RelicDefinition("ember_crown", "Ember Crown", RelicFamily.FIRE, RelicRarity.RARE, List.of("ember_aura"), List.of("flare_burst"), List.of("combat", "starter")));
        register(new RelicDefinition("tide_whisper", "Tide Whisper", RelicFamily.WATER, RelicRarity.UNCOMMON, List.of("soothing_current"), List.of("undertow_pulse"), List.of("support", "survival")));
        register(new RelicDefinition("storm_sigil", "Storm Sigil", RelicFamily.STORM, RelicRarity.EPIC, List.of("charged_sky"), List.of("thunder_lance"), List.of("burst", "mobility")));
        register(new RelicDefinition("abyssal_null", "Abyssal Null", RelicFamily.VOID, RelicRarity.MYTHIC, List.of("hollow_presence"), List.of("gravity_snare"), List.of("control", "late_game")));
        register(new RelicDefinition("radiant_heart", "Radiant Heart", RelicFamily.LIGHT, RelicRarity.RARE, List.of("dawn_aegis"), List.of("lumen_wave"), List.of("support", "defense")));
        register(new RelicDefinition("verdant_root", "Verdant Root", RelicFamily.NATURE, RelicRarity.COMMON, List.of("wild_growth"), List.of("thorn_spiral"), List.of("economy", "utility")));
        register(new RelicDefinition("granite_keystone", "Granite Keystone", RelicFamily.STONE, RelicRarity.UNCOMMON, List.of("steady_foundation"), List.of("seismic_line"), List.of("tank", "builder")));
        register(new RelicDefinition("astral_orbit", "Astral Orbit", RelicFamily.CELESTIAL, RelicRarity.LEGENDARY, List.of("star_map"), List.of("orbital_fall"), List.of("mobility", "precision")));
        register(new RelicDefinition("chronos_key", "Chronos Key", RelicFamily.TIME, RelicRarity.LEGENDARY, List.of("moment_stillness"), List.of("rewind_step"), List.of("tempo", "control")));
        register(new RelicDefinition("umbra_veil", "Umbra Veil", RelicFamily.SHADOW, RelicRarity.RARE, List.of("quiet_step"), List.of("veil_strike"), List.of("stealth", "assassin")));
        register(new RelicDefinition("chorus_bond", "Chorus Bond", RelicFamily.SUPPORT, RelicRarity.UNCOMMON, List.of("shared_breath"), List.of("binding_harmony"), List.of("teamplay", "healing")));
        register(new RelicDefinition("gilded_vault", "Gilded Vault", RelicFamily.ECONOMY, RelicRarity.RARE, List.of("coin_sense"), List.of("market_bloom"), List.of("trade", "growth")));
        register(new RelicDefinition("wayfinder_compass", "Wayfinder Compass", RelicFamily.EXPLORATION, RelicRarity.COMMON, List.of("map_memory"), List.of("trail_mark"), List.of("exploration", "travel")));
        register(new RelicDefinition("skyhook_lattice", "Skyhook Lattice", RelicFamily.MOBILITY, RelicRarity.EPIC, List.of("spring_anchor"), List.of("grapple_arc"), List.of("movement", "escape")));
        register(new RelicDefinition("forgebound_anvil", "Forgebound Anvil", RelicFamily.CRAFTING, RelicRarity.RARE, List.of("temper_reading"), List.of("refine_work"), List.of("crafting", "upgrade")));
        register(new RelicDefinition("waking_circle", "Waking Circle", RelicFamily.SUMMONER, RelicRarity.EPIC, List.of("summon_ritual"), List.of("echo_call"), List.of("minions", "control")));
        register(new RelicDefinition("blight_seed", "Blight Seed", RelicFamily.CORRUPTION, RelicRarity.MYTHIC, List.of("tainted_growth"), List.of("corrupt_surge"), List.of("risk", "power")));
    }

    private void register(RelicDefinition relicDefinition) {
        this.relics.put(relicDefinition.id(), relicDefinition);
    }

    @Override
    public Optional<RelicDefinition> findById(String relicId) {
        return Optional.ofNullable(this.relics.get(relicId));
    }

    @Override
    public List<RelicDefinition> allRelics() {
        ArrayList<RelicDefinition> relicList = new ArrayList<>(this.relics.values());
        relicList.sort(Comparator.comparing(RelicDefinition::displayName));
        return Collections.unmodifiableList(relicList);
    }

    @Override
    public PlayerRelicState createStartingState(String playerId, long seed) {
        Objects.requireNonNull(playerId, "playerId");
        RelicDefinition chosen = chooseStarter(seed);
        return new PlayerRelicState(
                playerId,
                chosen.id(),
                RelicTier.TIER_1,
                0,
                Map.of(),
                List.of(chosen.passiveIds().isEmpty() ? "starter_passive" : chosen.passiveIds().get(0))
        );
    }

    private RelicDefinition chooseStarter(long seed) {
        List<RelicDefinition> starterPool = this.allRelics().stream()
                .filter(relic -> relic.rarity() == RelicRarity.COMMON || relic.rarity() == RelicRarity.UNCOMMON || relic.rarity() == RelicRarity.RARE)
                .toList();
        if (starterPool.isEmpty()) {
            return this.allRelics().get(0);
        }
        Random random = new Random(seed);
        return starterPool.get(random.nextInt(starterPool.size()));
    }
}
