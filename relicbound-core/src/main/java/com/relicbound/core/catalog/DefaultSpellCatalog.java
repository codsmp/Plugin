package com.relicbound.core.catalog;

import com.relicbound.core.model.RelicFamily;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;
import com.relicbound.core.model.SpellEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultSpellCatalog implements SpellCatalog {
    private final Map<String, SpellDefinition> spells = new ConcurrentHashMap<>();

    public DefaultSpellCatalog() {
        register(new SpellDefinition("ember_burst", "Ember Burst", "spell_ember_burst", binding("BLAZE_POWDER", 2001), RelicTier.TIER_1, SpellEffectType.FIRE_CONE, List.of(RelicFamily.FIRE), 80, 3.5, 8.0, 0, "Blast nearby foes with a cone of flame.", 15, 0));
        register(new SpellDefinition("phoenix_dash", "Phoenix Dash", "spell_phoenix_dash", binding("FIRE_CHARGE", 2002), RelicTier.TIER_2, SpellEffectType.FIRE_DASH, List.of(RelicFamily.FIRE), 120, 2.0, 8.0, 0, "Dash forward in a burst of fire.", 20, 0));
        register(new SpellDefinition("tide_salve", "Tide Salve", "spell_tide_salve", binding("PRISMARINE_SHARD", 2101), RelicTier.TIER_1, SpellEffectType.WATER_HEAL, List.of(RelicFamily.WATER), 70, 4.0, 8.0, 0, "Restore health and cleanse weak burn effects.", 18, 0));
        register(new SpellDefinition("aquatic_blessing", "Aquatic Blessing", "spell_aquatic_blessing", binding("NAUTILUS_SHELL", 2103), RelicTier.TIER_1, SpellEffectType.AQUATIC_BLESSING, List.of(RelicFamily.WATER), 40, 0.0, 0.0, 100, "Bless yourself with water breathing and dolphin-like grace.", 12, 0));
        register(new SpellDefinition("undertow_wave", "Undertow Wave", "spell_undertow_wave", binding("HEART_OF_THE_SEA", 2102), RelicTier.TIER_2, SpellEffectType.WATER_WAVE, List.of(RelicFamily.WATER), 100, 2.5, 7.0, 0, "Knock back foes with a crushing surge.", 25, 0));
        register(new SpellDefinition("thunder_lance", "Thunder Lance", "spell_thunder_lance", binding("LIGHTNING_ROD", 2201), RelicTier.TIER_1, SpellEffectType.STORM_STRIKE, List.of(RelicFamily.STORM), 85, 6.5, 16.0, 0, "Strike a target with chain lightning.", 22, 0));
        register(new SpellDefinition("tempest_chain", "Tempest Chain", "spell_tempest_chain", binding("AMETHYST_SHARD", 2202), RelicTier.TIER_3, SpellEffectType.STORM_CHAIN, List.of(RelicFamily.STORM), 160, 4.5, 10.0, 0, "Arc lightning between enemies.", 35, 0));
        register(new SpellDefinition("gravity_snare", "Gravity Snare", "spell_gravity_snare", binding("ENDER_PEARL", 2301), RelicTier.TIER_2, SpellEffectType.VOID_PULL, List.of(RelicFamily.VOID), 110, 2.5, 8.0, 40, "Pull enemies into a void pocket.", 28, 3));
        register(new SpellDefinition("rift_step", "Rift Step", "spell_rift_step", binding("ENDER_EYE", 2302), RelicTier.TIER_1, SpellEffectType.VOID_BLINK, List.of(RelicFamily.VOID), 90, 2.0, 12.0, 0, "Blink through a tear in space.", 20, 0));
        register(new SpellDefinition("dawn_aegis", "Dawn Aegis", "spell_dawn_aegis", binding("GLOWSTONE_DUST", 2401), RelicTier.TIER_1, SpellEffectType.LIGHT_SHIELD, List.of(RelicFamily.LIGHT), 90, 6.0, 6.0, 120, "Wrap yourself in radiant protection.", 25, 5));
        register(new SpellDefinition("sanctify", "Sanctify", "spell_sanctify", binding("SEA_LANTERN", 2402), RelicTier.TIER_3, SpellEffectType.LIGHT_PURGE, List.of(RelicFamily.LIGHT), 130, 4.0, 7.0, 0, "Cleanse darkness and restore allies.", 30, 0));
        register(new SpellDefinition("root_bind", "Root Bind", "spell_root_bind", binding("OAK_SAPLING", 2501), RelicTier.TIER_1, SpellEffectType.NATURE_ROOT, List.of(RelicFamily.NATURE), 75, 3.0, 6.0, 60, "Anchor enemies in creeping roots.", 17, 2));
        register(new SpellDefinition("bloom_mend", "Bloom Mend", "spell_bloom_mend", binding("SPONGE", 2502), RelicTier.TIER_2, SpellEffectType.NATURE_HEAL, List.of(RelicFamily.NATURE), 95, 4.0, 7.0, 0, "Heal wounds with living growth.", 24, 0));
        register(new SpellDefinition("seismic_line", "Seismic Line", "spell_seismic_line", binding("STONE", 2601), RelicTier.TIER_1, SpellEffectType.STONE_RUMBLE, List.of(RelicFamily.STONE), 100, 4.0, 6.5, 0, "Send a shock through the ground.", 19, 0));
        register(new SpellDefinition("bulwark_wall", "Bulwark Wall", "spell_bulwark_wall", binding("BRICK", 2602), RelicTier.TIER_3, SpellEffectType.STONE_WALL, List.of(RelicFamily.STONE), 170, 2.0, 6.0, 120, "Raise a defensive stone barrier.", 32, 6));
        register(new SpellDefinition("starfall", "Starfall", "spell_starfall", binding("NETHER_STAR", 2701), RelicTier.TIER_3, SpellEffectType.CELESTIAL_FALL, List.of(RelicFamily.CELESTIAL), 180, 9.0, 14.0, 0, "Call down a beam from the heavens.", 40, 0));
        register(new SpellDefinition("astral_beacon", "Astral Beacon", "spell_astral_beacon", binding("END_CRYSTAL", 2702), RelicTier.TIER_2, SpellEffectType.CELESTIAL_BEACON, List.of(RelicFamily.CELESTIAL), 120, 3.5, 12.0, 160, "Reveal allies and guide their path.", 26, 4));
        register(new SpellDefinition("rewind_step", "Rewind Step", "spell_rewind_step", binding("CLOCK", 2801), RelicTier.TIER_2, SpellEffectType.TIME_REWIND, List.of(RelicFamily.TIME), 140, 2.0, 8.0, 0, "Snap back to a safer rhythm.", 30, 0));
        register(new SpellDefinition("slow_field", "Slow Field", "spell_slow_field", binding("FERMENTED_SPIDER_EYE", 2802), RelicTier.TIER_3, SpellEffectType.TIME_SLOW, List.of(RelicFamily.TIME), 180, 3.0, 7.0, 100, "Warp time around nearby foes.", 35, 5));
        register(new SpellDefinition("veil_strike", "Veil Strike", "spell_veil_strike", binding("INK_SAC", 2901), RelicTier.TIER_1, SpellEffectType.SHADOW_BURST, List.of(RelicFamily.SHADOW), 85, 4.0, 8.0, 0, "Ambush with a shadow lash.", 16, 0));
        register(new SpellDefinition("umbra_walk", "Umbra Walk", "spell_umbra_walk", binding("BLACK_DYE", 2902), RelicTier.TIER_2, SpellEffectType.SHADOW_VEIL, List.of(RelicFamily.SHADOW), 120, 2.5, 6.0, 60, "Fade into the dark for a brief escape.", 23, 3));
        register(new SpellDefinition("rally_chant", "Rally Chant", "spell_rally_chant", binding("GOAT_HORN", 3001), RelicTier.TIER_1, SpellEffectType.SUPPORT_RALLY, List.of(RelicFamily.SUPPORT), 90, 2.0, 8.0, 100, "Empower allies with a steadier pulse.", 21, 4));
        register(new SpellDefinition("life_tether", "Life Tether", "spell_life_tether", binding("GHAST_TEAR", 3002), RelicTier.TIER_3, SpellEffectType.SUPPORT_TETHER, List.of(RelicFamily.SUPPORT), 150, 3.0, 10.0, 80, "Share healing with nearby allies.", 38, 5));
        register(new SpellDefinition("coin_blessing", "Coin Blessing", "spell_coin_blessing", binding("GOLD_NUGGET", 3101), RelicTier.TIER_1, SpellEffectType.ECONOMY_BLESS, List.of(RelicFamily.ECONOMY), 80, 1.5, 6.0, 0, "Boost short-term luck and trade value.", 14, 0));
        register(new SpellDefinition("trail_reveal", "Trail Reveal", "spell_trail_reveal", binding("COMPASS", 3201), RelicTier.TIER_1, SpellEffectType.EXPLORATION_REVEAL, List.of(RelicFamily.EXPLORATION), 70, 2.0, 20.0, 0, "Reveal nearby untrusted players and points of interest.", 12, 0));
        register(new SpellDefinition("sky_leap", "Sky Leap", "spell_sky_leap", binding("FEATHER", 3301), RelicTier.TIER_1, SpellEffectType.MOBILITY_LEAP, List.of(RelicFamily.MOBILITY), 60, 3.0, 14.0, 0, "Launch upward and forward.", 18, 0));
        register(new SpellDefinition("temper_touch", "Temper Touch", "spell_temper_touch", binding("ANVIL", 3401), RelicTier.TIER_2, SpellEffectType.CRAFTING_TEMPER, List.of(RelicFamily.CRAFTING), 100, 2.0, 8.0, 60, "Sharpen and empower tools briefly.", 22, 3));
        register(new SpellDefinition("echo_call", "Echo Call", "spell_echo_call", binding("BONE", 3501), RelicTier.TIER_2, SpellEffectType.SUMMONER_CALL, List.of(RelicFamily.SUMMONER), 130, 3.0, 8.0, 120, "Summon echoes that point toward nearby untrusted players.", 28, 4));
        register(new SpellDefinition("blight_wave", "Blight Wave", "spell_blight_wave", binding("ROTTEN_FLESH", 3601), RelicTier.TIER_2, SpellEffectType.CORRUPTION_BLIGHT, List.of(RelicFamily.CORRUPTION), 120, 4.0, 6.5, 80, "Spread corruption in a diseased burst.", 26, 3));
        register(new SpellDefinition("abyss_rift", "Abyss Rift", "spell_abyss_rift", binding("CRYING_OBSIDIAN", 3602), RelicTier.TIER_4, SpellEffectType.CORRUPTION_RIFT, List.of(RelicFamily.CORRUPTION), 220, 6.0, 10.0, 120, "Rip open a dangerous breach of void energy.", 45, 0));
        register(new SpellDefinition("withering_cripple", "Withering Cripple", "spell_withering_cripple", binding("WITHER_ROSE", 3701), RelicTier.TIER_3, SpellEffectType.CORRUPTION_CRIPPLE, List.of(RelicFamily.CORRUPTION, RelicFamily.SHADOW), 20, 0.0, 6.0, 0, "Channel a crippling curse of darkness, slowness, and withering decay.", 0, 10));
        register(new SpellDefinition("meteor_surge", "Meteor Surge", "spell_meteor_surge", binding("FIRE_CHARGE", 3702), RelicTier.TIER_3, SpellEffectType.CELESTIAL_METEOR, List.of(RelicFamily.CELESTIAL, RelicFamily.STORM), 100, 2.5, 10.0, 0, "Call down a storm of meteors from above.", 30, 0));
        register(new SpellDefinition("meteor_rain", "Meteor Rain", "spell_meteor_rain", binding("NETHER_STAR", 3703), RelicTier.TIER_3, SpellEffectType.CELESTIAL_METEOR_RAIN, List.of(RelicFamily.CELESTIAL), 180, 3.0, 12.0, 0, "Rain meteors over a broad area.", 28, 0));
        register(new SpellDefinition("malfunction", "Malfunction", "spell_malfunction", binding("CLOCK", 3704), RelicTier.TIER_2, SpellEffectType.UTILITY_MALFUNCTION, List.of(RelicFamily.SHADOW, RelicFamily.VOID), 100, 0.0, 6.0, 0, "Scramble the target's tools and timing.", 14, 0));
        register(new SpellDefinition("lifedrain", "Lifedrain", "spell_lifedrain", binding("GHAST_TEAR", 3705), RelicTier.TIER_3, SpellEffectType.CORRUPTION_LIFEDRAIN, List.of(RelicFamily.CORRUPTION, RelicFamily.SHADOW), 160, 0.0, 7.0, 200, "Siphon stolen hearts from critical hits.", 20, 0));
        register(new SpellDefinition("lightning_charges", "Lightning Charges", "spell_lightning_charges", binding("LIGHTNING_ROD", 3706), RelicTier.TIER_3, SpellEffectType.STORM_CHARGES, List.of(RelicFamily.STORM), 160, 2.0, 12.0, 0, "Summon a barrage of lightning charges.", 18, 0));
        register(new SpellDefinition("frostbite", "Frostbite", "spell_frostbite", binding("PACKED_ICE", 3707), RelicTier.TIER_2, SpellEffectType.ELEMENTAL_FROSTBITE, List.of(RelicFamily.WATER, RelicFamily.STONE), 110, 0.0, 6.0, 120, "Freeze enemies and ice the ground around you.", 20, 0));
        register(new SpellDefinition("cooker", "Cooker", "spell_cooker", binding("FURNACE", 3708), RelicTier.TIER_1, SpellEffectType.ELEMENTAL_COOKER, List.of(RelicFamily.FIRE), 75, 1.5, 7.0, 0, "Seer the nearby area with scorching heat.", 15, 0));
        register(new SpellDefinition("gourmet", "Gourmet", "spell_gourmet", binding("COOKED_BEEF", 3709), RelicTier.TIER_3, SpellEffectType.ELEMENTAL_GOURMET, List.of(RelicFamily.NATURE, RelicFamily.SUPPORT), 150, 0.0, 7.0, 0, "Devour a creature to gain its boons.", 24, 0));
    }

    private static SpellDefinition.MaterialBinding binding(String material, Integer customModelData) {
        return new SpellDefinition.MaterialBinding(material, customModelData);
    }

    private void register(SpellDefinition spellDefinition) {
        this.spells.put(spellDefinition.id(), spellDefinition);
    }

    @Override
    public Optional<SpellDefinition> findById(String spellId) {
        return Optional.ofNullable(this.spells.get(spellId));
    }

    @Override
    public List<SpellDefinition> allSpells() {
        ArrayList<SpellDefinition> spellList = new ArrayList<>(this.spells.values());
        spellList.sort(Comparator.comparing(SpellDefinition::displayName));
        return Collections.unmodifiableList(spellList);
    }

    @Override
    public List<SpellDefinition> unlockableSpells(RelicFamily family, RelicTier tier) {
        List<SpellDefinition> results = new ArrayList<>();
        for (SpellDefinition spell : this.allSpells()) {
            boolean familyMatch = spell.affinities().isEmpty() || spell.affinities().contains(family);
            boolean tierMatch = tier.ordinal() >= spell.requiredTier().ordinal();
            if (familyMatch && tierMatch) {
                results.add(spell);
            }
        }
        return List.copyOf(results);
    }
}
