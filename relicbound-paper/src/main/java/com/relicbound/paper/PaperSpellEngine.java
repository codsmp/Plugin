package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.SpellDefinition;
import com.relicbound.core.model.SpellEffectType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Allay;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public final class PaperSpellEngine {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> channelTasks = new HashMap<>();
    private final Map<UUID, LifeDrainSession> lifeDrainSessions = new HashMap<>();
    private final Map<UUID, Long> noFallUntil = new HashMap<>();
    private final Map<UUID, Long> shadowVeilUntil = new HashMap<>();
    private final PlayerTrustStore trustStore;
    private final NamespacedKey spellStrengthBypassKey;
    private final java.util.Set<UUID> secretAPlusSkyLeap = new java.util.HashSet<>();

        // Balance values are runtime-configurable in config.yml under spell-balance.
        private final java.util.Set<com.relicbound.core.model.SpellEffectType> BOOSTED_EFFECTS = java.util.EnumSet.of(
            SpellEffectType.FIRE_CONE,
            SpellEffectType.FIRE_DASH,
            SpellEffectType.STORM_STRIKE,
            SpellEffectType.STORM_CHAIN,
            SpellEffectType.STORM_CHARGES,
            SpellEffectType.CELESTIAL_METEOR,
            SpellEffectType.CELESTIAL_METEOR_RAIN,
            SpellEffectType.CELESTIAL_FALL
        );

        private static final Set<SpellEffectType> FIRE_DAMAGE_EFFECTS = Set.of(
            SpellEffectType.FIRE_CONE,
            SpellEffectType.FIRE_DASH,
            SpellEffectType.ELEMENTAL_COOKER,
            SpellEffectType.CELESTIAL_METEOR,
            SpellEffectType.CELESTIAL_METEOR_RAIN
        );

        private static final Set<SpellEffectType> LIGHTNING_DAMAGE_EFFECTS = Set.of(
            SpellEffectType.STORM_STRIKE,
            SpellEffectType.STORM_CHAIN,
            SpellEffectType.STORM_CHARGES
        );

            // Intentionally varied meta by letter grade.
            private static final Set<String> A_PLUS_SPELLS = Set.of(
                "starfall",
                "meteor_rain",
                "abyss_rift"
            );

            private static final Set<String> A_SPELLS = Set.of(
                "thunder_lance",
                "dawn_aegis",
                "lifedrain"
            );

            private static final Set<String> A_MINUS_SPELLS = Set.of(
                "meteor_surge",
                "tempest_chain",
                "withering_cripple",
                "astral_beacon"
            );

            private static final Set<String> B_PLUS_SPELLS = Set.of(
                "ember_burst",
                "bloom_mend",
                "gravity_snare",
                "sanctify",
                "lightning_charges"
            );

            private static final Set<String> B_SPELLS = Set.of(
                "tide_salve",
                "seismic_line",
                "veil_strike",
                "rally_chant",
                "sky_leap"
            );

            private static final Set<String> B_MINUS_SPELLS = Set.of(
                "undertow_wave",
                "root_bind",
                "umbra_walk",
                "rewind_step",
                "slow_field"
            );

            private static final Set<String> C_PLUS_SPELLS = Set.of(
                "bulwark_wall",
                "life_tether",
                "frostbite",
                "echo_call"
            );

            private static final Set<String> C_MINUS_SPELLS = Set.of(
                "rift_step",
                "temper_touch",
                "malfunction"
            );

            private static final Set<String> D_PLUS_SPELLS = Set.of(
                "coin_blessing",
                "trail_reveal"
            );

            private static final Set<String> D_SPELLS = Set.of(
                "cooker",
                "gourmet"
            );

            private enum SpellBalanceGrade {
            A_PLUS("a-plus", 3.20D, 1.20D, 1.12D),
            A("a", 2.40D, 1.12D, 1.06D),
            A_MINUS("a-minus", 1.88D, 1.04D, 1.00D),
            B_PLUS("b-plus", 1.35D, 0.98D, 0.96D),
            B("b", 1.10D, 0.95D, 0.92D),
            B_MINUS("b-minus", 1.00D, 0.92D, 0.88D),
            C_PLUS("c-plus", 0.92D, 0.89D, 0.86D),
            C("c", 0.86D, 0.86D, 0.83D),
            C_MINUS("c-minus", 0.79D, 0.84D, 0.80D),
            D_PLUS("d-plus", 0.73D, 0.81D, 0.77D),
            D("d", 0.67D, 0.78D, 0.74D),
            D_MINUS("d-minus", 0.61D, 0.75D, 0.71D),
            F_PLUS("f-plus", 0.55D, 0.72D, 0.68D),
            F("f", 0.49D, 0.69D, 0.65D),
            F_MINUS("f-minus", 0.43D, 0.66D, 0.62D);

            private final String configKey;
            private final double defaultDamageMultiplier;
            private final double defaultCooldownMultiplier;
            private final double defaultManaMultiplier;

            SpellBalanceGrade(String configKey, double defaultDamageMultiplier, double defaultCooldownMultiplier, double defaultManaMultiplier) {
                this.configKey = configKey;
                this.defaultDamageMultiplier = defaultDamageMultiplier;
                this.defaultCooldownMultiplier = defaultCooldownMultiplier;
                this.defaultManaMultiplier = defaultManaMultiplier;
            }

            public String configKey() {
                return this.configKey;
            }

            public double defaultDamageMultiplier() {
                return this.defaultDamageMultiplier;
            }

            public double defaultCooldownMultiplier() {
                return this.defaultCooldownMultiplier;
            }

            public double defaultManaMultiplier() {
                return this.defaultManaMultiplier;
            }
            }

    public PaperSpellEngine(JavaPlugin plugin, RelicboundCore core, PlayerTrustStore trustStore) {
        this.plugin = plugin;
        this.core = core;
        this.trustStore = trustStore;
        this.spellStrengthBypassKey = new NamespacedKey(plugin, "spell_strength_bypass");
    }

    public void unlockAPlusSkyLeapFor(java.util.UUID playerId) {
        this.secretAPlusSkyLeap.add(playerId);
    }

    public boolean hasAPlusSkyLeap(java.util.UUID playerId) {
        return this.secretAPlusSkyLeap.contains(playerId);
    }

    public boolean cast(Player player, SpellDefinition spellDefinition) {
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), player.getUniqueId().getMostSignificantBits());
        if (!state.unlockedAbilities().contains(spellDefinition.id())) {
            throw new IllegalStateException("That spell is not unlocked yet. Open your relic menu to unlock more spells.");
        }
        PlayerManaState manaState = this.core.getPlayerManaState(player.getUniqueId().toString())
            .orElseGet(() -> StarterItemUtil.findAnyStarterArchetype(player)
                .map(archetype -> this.core.getOrCreatePlayerManaState(player.getUniqueId().toString(), archetype))
                .orElseThrow(() -> new IllegalStateException("Choose an archetype before casting spells.")));
        manaState = this.core.updateManaRegen(manaState, System.currentTimeMillis());
        manaState = this.core.savePlayerManaState(manaState);
        long now = System.currentTimeMillis();
        long readyAt = this.cooldowns.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>())
                .getOrDefault(spellDefinition.id(), 0L);
        if (now < readyAt) {
            long secondsLeft = Math.max(1L, (readyAt - now + 999L) / 1000L);
            throw new IllegalStateException(spellDefinition.displayName() + " is on cooldown for " + secondsLeft + "s.");
        }
        int manaCost = this.scaledManaCost(spellDefinition, manaState);
        if (manaState.currentMana() < manaCost) {
            throw new IllegalStateException("Not enough mana for " + spellDefinition.displayName() + ". You have " + manaState.currentMana() + "/" + manaCost + ".");
        }
        manaState = this.core.drainMana(manaState, manaCost);
        manaState = this.core.savePlayerManaState(manaState);
        int cooldownTicks = this.scaledCooldownTicks(spellDefinition, manaState);
        this.cooldowns.get(player.getUniqueId()).put(spellDefinition.id(), now + (cooldownTicks * 50L));
        switch (spellDefinition.effectType()) {
            case CORRUPTION_CRIPPLE -> this.startChannelSpell(player, spellDefinition);
            case CORRUPTION_LIFEDRAIN -> this.beginLifeDrain(player, spellDefinition, manaState);
            default -> this.applyInstantSpell(player, spellDefinition, manaState);
        }
        player.sendActionBar(ChatColor.AQUA + spellDefinition.displayName() + ChatColor.GRAY + " cast. Mana: " + ChatColor.WHITE + manaState.currentMana() + ChatColor.GRAY + "/" + ChatColor.WHITE + manaState.maxMana());
        // Secret: if player has the A+ Sky Leap unlock, append Sky Leap effect to any A+ cast
        try {
            if (this.resolveGrade(spellDefinition) == SpellBalanceGrade.A_PLUS && this.hasAPlusSkyLeap(player.getUniqueId())) {
                this.core.findSpell("sky_leap").ifPresent(s -> this.mobilityLeap(player, s.power(), s.range()));
            }
        } catch (Exception ignored) {
            // Keep this behavior silent and fail-safe for secrecy
        }
        return true;
    }

    public long remainingCooldownSeconds(Player player, SpellDefinition spellDefinition) {
        long now = System.currentTimeMillis();
        long readyAt = this.cooldowns.getOrDefault(player.getUniqueId(), Map.of())
                .getOrDefault(spellDefinition.id(), 0L);
        if (readyAt <= now) {
            return 0L;
        }
        return Math.max(1L, (readyAt - now + 999L) / 1000L);
    }

    private void startChannelSpell(Player player, SpellDefinition spell) {
        UUID playerId = player.getUniqueId();
        BukkitTask existingTask = this.channelTasks.remove(playerId);
        if (existingTask != null) {
            existingTask.cancel();
        }

        if (!this.tickChannelSpell(player, spell)) {
            return;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (!player.isOnline() || !this.tickChannelSpell(player, spell)) {
                this.cancelChannel(playerId);
            }
        }, 20L, 20L);
        this.channelTasks.put(playerId, task);
    }

    private void cancelChannel(UUID playerId) {
        BukkitTask task = this.channelTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    private void beginLifeDrain(Player player, SpellDefinition spell, PlayerManaState manaState) {
        UUID playerId = player.getUniqueId();
        LifeDrainSession existing = this.lifeDrainSessions.remove(playerId);
        if (existing != null) {
            existing.cancel();
        }

        LifeDrainSession session = new LifeDrainSession(playerId, spell.id(), manaState.archetype());
        this.lifeDrainSessions.put(playerId, session);
        session.startFinalizeTask(this.plugin, () -> this.finalizeLifeDrain(playerId));
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 24, 0.4, 0.6, 0.4, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7F, 1.2F);
    }

    public boolean handleLifeDrainCriticalHit(Player attacker, LivingEntity target) {
        LifeDrainSession session = this.lifeDrainSessions.get(attacker.getUniqueId());
        if (session == null || session.locked || session.expired()) {
            return false;
        }
        if (session.stolenHealthPoints >= 10) {
            return false;
        }

        // Drain 2 hearts (4.0 health points) per critical hit and cap at 10 steals
        target.setHealth(Math.max(0.0D, target.getHealth() - 4.0D));
        session.stolenHealthPoints = Math.min(10, session.stolenHealthPoints + 1);
        attacker.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 10, 0.3, 0.4, 0.3, 0.02);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7F, 0.8F);
        return true;
    }

    private void finalizeLifeDrain(UUID playerId) {
        LifeDrainSession session = this.lifeDrainSessions.get(playerId);
        if (session == null || session.locked) {
            return;
        }

        session.locked = true;
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        // Grant absorption equal to drained health (4.0 per steal)
        player.setAbsorptionAmount(session.stolenHealthPoints * 4.0D);
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation(), 20, 0.4, 0.5, 0.4, 0.04);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            LifeDrainSession current = this.lifeDrainSessions.remove(playerId);
            Player online = Bukkit.getPlayer(playerId);
            if (online != null && online.isOnline()) {
                online.setAbsorptionAmount(0.0D);
            }
            if (current != null) {
                current.cancel();
            }
        }, 15L * 20L);
    }

    private boolean tickChannelSpell(Player player, SpellDefinition spell) {
        PlayerManaState manaState = this.core.getPlayerManaState(player.getUniqueId().toString()).orElse(null);
        if (manaState == null) {
            return false;
        }

        manaState = this.core.updateManaRegen(manaState, System.currentTimeMillis());
        manaState = this.core.savePlayerManaState(manaState);
        if (manaState.currentMana() <= 0) {
            return false;
        }

        this.applyChannelSpell(player, spell, manaState);

        int drainAmount = this.effectiveChannelDrain(spell, manaState);
        manaState = this.core.drainMana(manaState, drainAmount);
        this.core.savePlayerManaState(manaState);
        return manaState.currentMana() > 0;
    }

    private void applyChannelSpell(Player player, SpellDefinition spell, PlayerManaState manaState) {
        switch (spell.effectType()) {
            case CORRUPTION_CRIPPLE -> this.witheringCripple(player, manaState);
            default -> this.applyInstantSpell(player, spell, manaState);
        }
    }

    private void applyInstantSpell(Player player, SpellDefinition spell, PlayerManaState manaState) {
        SpellEffectType effect = spell.effectType();
        Location origin = player.getLocation();
        this.epicCastBurst(player, spell, manaState);
        double scaledPower = this.scaledDamage(spell, manaState);
        switch (effect) {
            case FIRE_CONE -> this.emberBurst(player, scaledPower, effectiveRange(spell.range()), manaState.archetype());
            case FIRE_DASH -> this.dashForward(player, effectiveRange(spell.range()), 0.8D, true);
            case WATER_HEAL -> this.tideSalve(player, spell.power(), effectiveRange(spell.range()));
            case AQUATIC_BLESSING -> this.aquaticBlessing(player, spell.durationTicks());
            case WATER_WAVE -> this.knockbackNearby(player, scaledPower, effectiveRange(spell.range()), false);
            case STORM_STRIKE -> this.thunderLance(player, scaledPower, effectiveRange(spell.range()), manaState.archetype());
            case STORM_CHAIN -> this.tempestChain(player, scaledPower, effectiveRange(spell.range()), manaState.archetype());
            case VOID_PULL -> this.gravitySnare(player, scaledPower, effectiveRange(spell.range()), manaState.archetype());
            case VOID_BLINK -> this.blinkForward(player, effectiveRange(spell.range()));
            case LIGHT_SHIELD -> this.shieldSelf(player, spell.id(), spell.power(), spell.durationTicks());
            case LIGHT_PURGE -> this.purgeAndHeal(player, spell.power(), effectiveRange(spell.range()));
            case NATURE_ROOT -> this.rootNearby(player, scaledPower, effectiveRange(spell.range()));
            case NATURE_HEAL -> this.areaHeal(player, spell.power(), effectiveRange(spell.range()));
            case STONE_RUMBLE -> this.knockbackNearby(player, scaledPower, effectiveRange(spell.range()), true);
            case STONE_WALL -> this.stoneBulwark(player, spell.power(), effectiveRange(spell.range()));
            case CELESTIAL_FALL -> this.celestialSmite(player, scaledPower, effectiveRange(spell.range()));
            case CELESTIAL_BEACON -> this.beaconPulse(player, spell.power(), effectiveRange(spell.range()), spell.durationTicks());
            case TIME_REWIND -> this.rewind(player, effectiveRange(spell.range()), spell.durationTicks());
            case TIME_SLOW -> this.timeSlow(player, spell.power(), effectiveRange(spell.range()), spell.durationTicks());
            case SHADOW_VEIL -> this.shadowVeil(player, spell.power(), spell.durationTicks());
            case SHADOW_BURST -> this.shadowBurst(player, scaledPower, effectiveRange(spell.range()));
            case SUPPORT_RALLY -> this.rally(player, spell.power(), effectiveRange(spell.range()), spell.durationTicks());
            case SUPPORT_TETHER -> this.tether(player, spell.power(), effectiveRange(spell.range()), spell.durationTicks());
            case ECONOMY_BLESS -> this.economyBless(player, spell.power());
            case EXPLORATION_REVEAL -> this.explorationReveal(player, effectiveRange(spell.range()), spell.durationTicks());
            case MOBILITY_LEAP -> this.mobilityLeap(player, spell.power(), spell.range());
            case CRAFTING_TEMPER -> this.craftingTemper(player, spell.power(), spell.durationTicks());
            case SUMMONER_CALL -> this.summonHelper(player, spell.power());
            case CORRUPTION_BLIGHT -> this.corruptionBlight(player, scaledPower, spell.range(), spell.durationTicks());
            case CORRUPTION_RIFT -> this.corruptionRift(player, scaledPower, spell.range(), spell.durationTicks());
            case CORRUPTION_CRIPPLE -> this.witheringCripple(player, manaState);
            case ELEMENTAL_FROSTBITE -> this.frostbite(player, manaState);
            case ELEMENTAL_COOKER -> this.cooker(player, spell, manaState);
            case ELEMENTAL_GOURMET -> this.gourmet(player, spell, manaState);
            case CELESTIAL_METEOR -> this.meteorStorm(player, spell, manaState);
            case CELESTIAL_METEOR_RAIN -> this.meteorRain(player, spell, manaState);
            case UTILITY_MALFUNCTION -> this.malfunction(player, spell, manaState);
            case STORM_CHARGES -> this.lightningCharges(player, spell, manaState);
            case CORRUPTION_LIFEDRAIN -> this.beginLifeDrain(player, spell, manaState);
        }
    }

    private void epicCastBurst(Player player, SpellDefinition spell, PlayerManaState manaState) {
        Location origin = player.getLocation().clone().add(0, 1.0D, 0);
        Particle.DustOptions accent = new Particle.DustOptions(Color.fromRGB(120, 200, 255), 1.3F);
        Particle.DustOptions warm = new Particle.DustOptions(Color.fromRGB(255, 140, 70), 1.2F);

        player.getWorld().spawnParticle(Particle.END_ROD, origin, 18, 0.35, 0.45, 0.35, 0.05);
        player.getWorld().spawnParticle(Particle.DUST, origin, 12, 0.25, 0.35, 0.25, 0.02, spell.effectType().name().contains("FIRE") ? warm : accent);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, origin, 10, 0.3, 0.35, 0.3, 0.04);
        if (spell.effectType().name().contains("CORRUPTION") || spell.effectType().name().contains("SHADOW")) {
            player.getWorld().spawnParticle(Particle.WITCH, origin, 14, 0.3, 0.45, 0.3, 0.03);
        }
        if (spell.effectType().name().contains("WATER") || spell.effectType().name().contains("HEAL") || spell.effectType().name().contains("LIGHT")) {
            player.getWorld().spawnParticle(Particle.SPLASH, origin, 14, 0.35, 0.35, 0.35, 0.04);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, origin, 8, 0.25, 0.25, 0.25, 0.02);
        }

        Sound sound = switch (spell.effectType()) {
            case FIRE_CONE, FIRE_DASH -> Sound.ENTITY_BLAZE_SHOOT;
            case WATER_HEAL, WATER_WAVE -> Sound.ITEM_TRIDENT_RIPTIDE_3;
            case AQUATIC_BLESSING -> Sound.BLOCK_CONDUIT_AMBIENT;
            case STORM_STRIKE, STORM_CHAIN, STORM_CHARGES -> Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            case VOID_PULL, VOID_BLINK -> Sound.ENTITY_ENDERMAN_TELEPORT;
            case LIGHT_SHIELD, LIGHT_PURGE, CELESTIAL_BEACON, CELESTIAL_FALL -> Sound.BLOCK_BEACON_POWER_SELECT;
            case NATURE_ROOT, NATURE_HEAL -> Sound.BLOCK_AZALEA_LEAVES_PLACE;
            case STONE_RUMBLE, STONE_WALL -> Sound.BLOCK_STONE_BREAK;
            case TIME_REWIND, TIME_SLOW -> Sound.BLOCK_AMETHYST_BLOCK_CHIME;
            case SHADOW_VEIL, SHADOW_BURST -> Sound.ENTITY_WITHER_AMBIENT;
            case SUPPORT_RALLY, SUPPORT_TETHER -> Sound.BLOCK_BELL_USE;
            case ECONOMY_BLESS -> Sound.BLOCK_NOTE_BLOCK_BELL;
            case EXPLORATION_REVEAL -> Sound.BLOCK_AMETHYST_CLUSTER_HIT;
            case MOBILITY_LEAP -> Sound.ENTITY_ENDER_DRAGON_FLAP;
            case CRAFTING_TEMPER -> Sound.BLOCK_ANVIL_USE;
            case SUMMONER_CALL -> Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM;
            case CORRUPTION_BLIGHT, CORRUPTION_RIFT, CORRUPTION_CRIPPLE, CORRUPTION_LIFEDRAIN -> Sound.ENTITY_WITHER_SPAWN;
            case UTILITY_MALFUNCTION -> Sound.ENTITY_ENDERMAN_TELEPORT;
            case ELEMENTAL_FROSTBITE -> Sound.BLOCK_GLASS_BREAK;
            case ELEMENTAL_COOKER -> Sound.ITEM_FIRECHARGE_USE;
            case ELEMENTAL_GOURMET -> Sound.ENTITY_GENERIC_EAT;
            case CELESTIAL_METEOR, CELESTIAL_METEOR_RAIN -> Sound.ENTITY_GENERIC_EXPLODE;
        };
        float pitch = manaState.archetype() == PlayerArchetype.STAFF ? 0.9F : 1.2F;
        player.getWorld().playSound(origin, sound, 1.0F, pitch);
    }

    private int effectiveChannelDrain(SpellDefinition spell, PlayerManaState manaState) {
        PlayerArchetype archetype = manaState.archetype();
        if (spell.effectType() == SpellEffectType.CORRUPTION_CRIPPLE) {
            return archetype == PlayerArchetype.STAFF ? 20 : 10;
        }
        double base = spell.manaPerSecond() * archetype.manaDrainMultiplier();
        base *= this.tierManaMultiplierForPlayer(manaState.playerId());
        return Math.max(1, (int) Math.round(base));
    }

    private int scaledCooldownTicks(SpellDefinition spell, PlayerManaState manaState) {
        PlayerArchetype archetype = manaState.archetype();
        double base = spell.cooldownTicks() * this.globalCooldownMultiplier() / archetype.castSpeedMultiplier();
        base *= this.gradeCooldownMultiplier(this.resolveGrade(spell));
        base *= this.tierCooldownMultiplierForPlayer(manaState.playerId());
        return Math.max(1, (int) Math.round(base));
    }

    private int scaledManaCost(SpellDefinition spell, PlayerManaState manaState) {
        PlayerArchetype archetype = manaState.archetype();
        double base = spell.manaCost() * archetype.manaDrainMultiplier();
        base *= this.gradeManaMultiplier(this.resolveGrade(spell));
        base *= this.tierManaMultiplierForPlayer(manaState.playerId());
        return Math.max(0, (int) Math.round(base));
    }

    private double scaledDamage(SpellDefinition spell, PlayerManaState manaState) {
        PlayerArchetype archetype = manaState.archetype();
        double base = spell.power() * archetype.damageMultiplier() * this.globalDamageMultiplier();
        base *= this.elementalDamageMultiplier(spell.effectType());
        if (this.BOOSTED_EFFECTS.contains(spell.effectType())) {
            double boost = this.boostedEffectMultiplier();
            if (archetype == PlayerArchetype.STAFF) {
                boost *= this.boostedEffectStaffExtraMultiplier();
            }
            double result = base * boost * this.gradeDamageMultiplier(this.resolveGrade(spell));
            result *= this.tierDamageMultiplierForPlayer(manaState.playerId());
            // Guarantee A+ spells scale to exceed configured A+ minimum after global buff
            if (this.resolveGrade(spell) == SpellBalanceGrade.A_PLUS) {
                double minScaled = 12.01D / this.globalSpellBuff();
                return Math.max(result, minScaled);
            }
            return result;
        }
        double result = base * this.gradeDamageMultiplier(this.resolveGrade(spell));
        result *= this.tierDamageMultiplierForPlayer(manaState.playerId());
        if (this.resolveGrade(spell) == SpellBalanceGrade.A_PLUS) {
            double minScaled = 12.01D / this.globalSpellBuff();
            return Math.max(result, minScaled);
        }
        return result;
    }

    private double tierDamageMultiplierForPlayer(String playerId) {
        return this.core.findPlayerState(playerId)
                .map(state -> switch (state.tier()) {
                    case TIER_2 -> 1.05D;
                    case TIER_3 -> 1.05D;
                    case TIER_4 -> 1.15D;
                    case TIER_5 -> 1.25D;
                    case ASCENSION -> 1.35D;
                    default -> 1.0D;
                }).orElse(1.0D);
    }

    private double tierCooldownMultiplierForPlayer(String playerId) {
        return this.core.findPlayerState(playerId)
                .map(state -> switch (state.tier()) {
                    case TIER_2 -> 0.95D;
                    case TIER_5 -> 0.90D;
                    case ASCENSION -> 0.90D;
                    default -> 1.0D;
                }).orElse(1.0D);
    }

    private double tierManaMultiplierForPlayer(String playerId) {
        return this.core.findPlayerState(playerId)
                .map(state -> switch (state.tier()) {
                    case TIER_3 -> 0.90D;
                    case TIER_4 -> 0.90D;
                    case TIER_5 -> 0.90D;
                    case ASCENSION -> 0.85D;
                    default -> 1.0D;
                }).orElse(1.0D);
    }

    private double effectiveRange(double range) {
        return range * this.globalRangeMultiplier();
    }

    private void damageNearby(Player player, double damage, double range, boolean fire, boolean wither) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                if (this.isTrustedTarget(player, living)) {
                    continue;
                }
                applyMinimumTrueDamage(living, damage, player);
                if (fire) {
                    living.setFireTicks(60);
                }
                if (wither) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 0, true, true, true));
                }
            }
        }
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 32, 0.8, 0.6, 0.8, 0.05);
    }

    private void healSelf(Player player, double amount, boolean particles) {
        double maxHealth = this.maxHealth(player);
        player.setHealth(Math.min(maxHealth, player.getHealth() + amount));
        if (particles) {
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 8, 0.4, 0.4, 0.4, 0.03);
        }
    }

    private void dashForward(Player player, double range, double verticalBoost, boolean fireTrail) {
        Vector direction = player.getLocation().getDirection().normalize().multiply(Math.max(0.8D, range / 4.0D));
        direction.setY(verticalBoost);
        player.setVelocity(direction);
        if (fireTrail) {
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation(), 24, 0.4, 0.5, 0.4, 0.05);
        }
    }

    private void knockbackNearby(Player player, double power, double range, boolean seismic) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                if (this.isTrustedTarget(player, living)) {
                    continue;
                }
                Vector knockback = living.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(Math.max(0.2D, power / 6.0D));
                knockback.setY(0.35D);
                living.setVelocity(knockback);
                applyMinimumTrueDamage(living, Math.max(2.0D, power), player);
            }
        }
        player.getWorld().spawnParticle(seismic ? Particle.BLOCK_CRUMBLE : Particle.SPLASH, player.getLocation(), 28, 1.0, 0.4, 1.0, 0.12);
    }

    private void strikeNearest(Player player, double damage, double range, boolean lightning) {
        LivingEntity target = this.nearestLiving(player, range);
        if (target == null) {
            return;
        }
        if (this.isTrustedTarget(player, target)) {
            return;
        }
        applyMinimumTrueDamage(target, damage, player);
        if (lightning) {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }
    }

    private void chainStrike(Player player, double damage, double range) {
        int hits = 0;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (hits >= 3) {
                break;
            }
            if (entity instanceof LivingEntity living && living != player) {
                if (this.isTrustedTarget(player, living)) {
                    continue;
                }
                applyMinimumTrueDamage(living, damage, player);
                living.getWorld().strikeLightningEffect(living.getLocation());
                hits++;
            }
        }
    }

    private void emberBurst(Player player, double damage, double range, PlayerArchetype archetype) {
        // Cone parameters differ between wand and staff
        double coneDegrees = archetype == PlayerArchetype.STAFF ? 90.0D : 45.0D;
        double coneCos = Math.cos(Math.toRadians(coneDegrees / 2.0D));
        Vector dir = player.getLocation().getDirection().normalize();
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof LivingEntity living) || living == player) continue;
            if (this.isTrustedTarget(player, living)) continue;
            Vector to = living.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            double dot = dir.dot(to);
            if (dot >= coneCos) {
                applyMinimumTrueDamage(living, Math.max(2.0D, damage), player);
                int fireTicks = archetype == PlayerArchetype.STAFF ? 120 : 60;
                living.setFireTicks(Math.max(living.getFireTicks(), fireTicks));
            }
        }
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 48, 0.9, 0.8, 0.9, 0.06);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 16, 0.6, 0.4, 0.6, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.1F, archetype == PlayerArchetype.STAFF ? 0.9F : 1.2F);
    }

    private void thunderLance(Player player, double damage, double range, PlayerArchetype archetype) {
        LivingEntity target = this.nearestLiving(player, range);
        if (target == null) return;
        target.getWorld().strikeLightningEffect(target.getLocation());
        applyMinimumTrueDamage(target, damage + (archetype == PlayerArchetype.STAFF ? 2.0D : 0.0D), player);
        // small chain effect to nearby enemies
        int chainJumps = archetype == PlayerArchetype.STAFF ? 3 : 1;
        double chainRange = archetype == PlayerArchetype.STAFF ? 6.0D : 3.0D;
        List<LivingEntity> chained = new ArrayList<>();
        chained.add(target);
        LivingEntity last = target;
        for (int i = 0; i < chainJumps; i++) {
            LivingEntity next = null;
            double nearestDist = Double.MAX_VALUE;
            for (Entity e : last.getNearbyEntities(chainRange, chainRange, chainRange)) {
                if (e instanceof LivingEntity l && l != player && !chained.contains(l)) {
                    double d = l.getLocation().distanceSquared(last.getLocation());
                    if (d < nearestDist) {
                        nearestDist = d;
                        next = l;
                    }
                }
            }
            if (next == null) break;
            next.getWorld().strikeLightningEffect(next.getLocation());
            applyMinimumTrueDamage(next, damage * 0.8D, player);
            next.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, next.getLocation(), 20, 0.4, 0.6, 0.4, 0.02);
            next.getWorld().playSound(next.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9F, 1.0F);
            chained.add(next);
            last = next;
        }
    }

    private void tempestChain(Player player, double damage, double range, PlayerArchetype archetype) {
        int jumps = archetype == PlayerArchetype.STAFF ? 6 : 3;
        int hits = 0;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (hits >= jumps) break;
            if (entity instanceof LivingEntity living && living != player) {
                applyMinimumTrueDamage(living, damage, player);
                living.getWorld().strikeLightningEffect(living.getLocation());
                living.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, living.getLocation().add(0, 1, 0), 16, 0.3, 0.4, 0.3, 0.02);
                living.getWorld().playSound(living.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9F, 1.0F);
                hits++;
            }
        }
    }

    private void gravitySnare(Player player, double damage, double range, PlayerArchetype archetype) {
        double pullStrength = archetype == PlayerArchetype.STAFF ? 1.2D : 0.6D;
        int slowTicks = archetype == PlayerArchetype.STAFF ? 100 : 60;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                Vector pull = player.getLocation().toVector().subtract(living.getLocation().toVector()).normalize().multiply(pullStrength);
                pull.setY(0.2D);
                living.setVelocity(pull);
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowTicks, 2, true, true, true));
                applyMinimumTrueDamage(living, Math.max(2.0D, damage), player);
            }
        }
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 30, 0.6, 0.8, 0.6, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.9F);
    }

    private void pullNearby(Player player, double damage, double range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                Vector pull = player.getLocation().toVector().subtract(living.getLocation().toVector()).normalize().multiply(0.8D);
                pull.setY(0.2D);
                living.setVelocity(pull);
                applyMinimumTrueDamage(living, Math.max(2.0D, damage), player);
            }
        }
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 30, 0.6, 0.8, 0.6, 0.2);
    }

    private void blinkForward(Player player, double range) {
        Location target = player.getLocation().clone().add(player.getLocation().getDirection().normalize().multiply(range));
        target.setPitch(player.getLocation().getPitch());
        target.setYaw(player.getLocation().getYaw());
        if (target.getBlock().isPassable()) {
            player.teleport(target);
        }
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation(), 32, 0.6, 0.8, 0.6, 0.1);
    }

    private void shieldSelf(Player player, String spellId, double amount, int durationTicks) {
        int resolvedDurationTicks = this.resolveShieldDurationTicks(spellId, durationTicks);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, resolvedDurationTicks, 0, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, resolvedDurationTicks, Math.max(0, (int) Math.round(amount / 4.0D)), true, true, true));
    }

    private int resolveShieldDurationTicks(String spellId, int fallbackTicks) {
        if (!"dawn_aegis".equalsIgnoreCase(spellId)) {
            return fallbackTicks;
        }
        return Math.max(20, this.plugin.getConfig().getInt("spell-balance.dawn-aegis-duration-ticks", fallbackTicks));
    }

    private double globalDamageMultiplier() {
        return this.boundedDouble("spell-balance.global-damage-multiplier", 1.5D, 0.1D, 20.0D);
    }

    private double globalCooldownMultiplier() {
        return this.boundedDouble("spell-balance.global-cooldown-multiplier", 0.9D, 0.1D, 5.0D);
    }

    private double globalRangeMultiplier() {
        return this.boundedDouble("spell-balance.global-range-multiplier", 1.1D, 0.1D, 5.0D);
    }

    private double boostedEffectMultiplier() {
        return this.boundedDouble("spell-balance.boosted-effect-multiplier", 2.0D, 0.1D, 20.0D);
    }

    private double boostedEffectStaffExtraMultiplier() {
        return this.boundedDouble("spell-balance.boosted-effect-staff-extra-multiplier", 1.25D, 0.1D, 10.0D);
    }

    private double fireDamageMultiplier() {
        return this.boundedDouble("spell-balance.school-damage.fire-multiplier", 1.15D, 0.1D, 10.0D);
    }

    private double lightningDamageMultiplier() {
        return this.boundedDouble("spell-balance.school-damage.lightning-multiplier", 1.20D, 0.1D, 10.0D);
    }

    private double boundedDouble(String path, double fallback, double min, double max) {
        double value = this.plugin.getConfig().getDouble(path, fallback);
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    private SpellBalanceGrade resolveGrade(SpellDefinition spell) {
        String spellId = spell.id();
        if (A_PLUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.A_PLUS;
        }
        if (A_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.A;
        }
        if (A_MINUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.A_MINUS;
        }
        if (B_PLUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.B_PLUS;
        }
        if (B_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.B;
        }
        if (B_MINUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.B_MINUS;
        }
        if (C_PLUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.C_PLUS;
        }
        if (C_MINUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.C_MINUS;
        }
        if (D_PLUS_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.D_PLUS;
        }
        if (D_SPELLS.contains(spellId)) {
            return SpellBalanceGrade.D;
        }
        return SpellBalanceGrade.C;
    }

    private double gradeDamageMultiplier(SpellBalanceGrade grade) {
        return this.boundedDouble(
                "spell-balance.grades." + grade.configKey() + ".damage-multiplier",
                grade.defaultDamageMultiplier(),
                0.1D,
                10.0D
        );
    }

    private double gradeCooldownMultiplier(SpellBalanceGrade grade) {
        return this.boundedDouble(
                "spell-balance.grades." + grade.configKey() + ".cooldown-multiplier",
                grade.defaultCooldownMultiplier(),
                0.1D,
                10.0D
        );
    }

    private double gradeManaMultiplier(SpellBalanceGrade grade) {
        return this.boundedDouble(
                "spell-balance.grades." + grade.configKey() + ".mana-multiplier",
                grade.defaultManaMultiplier(),
                0.1D,
                10.0D
        );
    }

    private double elementalDamageMultiplier(SpellEffectType effectType) {
        if (FIRE_DAMAGE_EFFECTS.contains(effectType)) {
            return this.fireDamageMultiplier();
        }
        if (LIGHTNING_DAMAGE_EFFECTS.contains(effectType)) {
            return this.lightningDamageMultiplier();
        }
        return 1.0D;
    }

    private void purgeAndHeal(Player player, double amount, double range) {
        this.areaHeal(player, amount, range);
        player.getActivePotionEffects().forEach(effect -> {
            if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.WITHER) || effect.getType().equals(PotionEffectType.BLINDNESS)) {
                player.removePotionEffect(effect.getType());
            }
        });
    }

    private void tideSalve(Player player, double amount, double range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living) {
                if (!canReceiveHealing(player, living)) continue;
                double maxHealth = this.maxHealth(living);
                living.setHealth(Math.min(maxHealth, living.getHealth() + amount));
                living.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, true, true, true));
                // Remove weaker debuffs: weakness and small burns/poison; extinguish fire ticks
                living.removePotionEffect(PotionEffectType.WEAKNESS);
                living.removePotionEffect(PotionEffectType.POISON);
                // Wither is more severe; remove only short instances
                PotionEffect wither = living.getPotionEffect(PotionEffectType.WITHER);
                if (wither != null && wither.getDuration() <= 100) {
                    living.removePotionEffect(PotionEffectType.WITHER);
                }
                living.setFireTicks(0);
            }
        }
        // Also heal the caster and show particles
        this.healSelf(player, amount, true);
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 24, 0.6, 0.6, 0.6, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.9F, 1.0F);
    }

    private void applySpellStrength(LivingEntity target, int durationTicks, int amplifier, boolean ambient, boolean particles, boolean icon) {
        if (target instanceof Player player) {
            player.getPersistentDataContainer().set(this.spellStrengthBypassKey, PersistentDataType.BYTE, (byte) 1);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                if (player.isOnline()) {
                    player.getPersistentDataContainer().remove(this.spellStrengthBypassKey);
                }
            }, 1L);
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durationTicks, amplifier, ambient, particles, icon));
    }

    private void aquaticBlessing(Player player, int durationTicks) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, durationTicks, 0, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, durationTicks, 0, true, true, true));
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 20, 0.45, 0.45, 0.45, 0.04);
        player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, player.getLocation(), 12, 0.35, 0.55, 0.35, 0.03);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 0.8F, 1.2F);
    }

    private void rootNearby(Player player, double damage, double range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                applyMinimumTrueDamage(living, Math.max(2.0D, damage), player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255, true, true, true));
            }
        }
    }

    private void areaHeal(Player player, double amount, double range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living) {
                if (!canReceiveHealing(player, living)) continue;
                double maxHealth = this.maxHealth(living);
                living.setHealth(Math.min(maxHealth, living.getHealth() + amount));
                living.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, true, true, true));
            }
        }
        this.healSelf(player, amount, true);
    }

    private void stoneBulwark(Player player, double amount, double range) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1, true, true, true));
        player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 40, 0.9, 0.6, 0.9, 0.2);
        this.knockbackNearby(player, amount, range, true);
    }

    private void celestialSmite(Player player, double damage, double range) {
        LivingEntity target = this.nearestLiving(player, range);
        if (target != null && !this.isTrustedTarget(player, target)) {
            target.getWorld().strikeLightningEffect(target.getLocation());
            applyMinimumTrueDamage(target, damage + 2.0D, player);
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, true, true, true));
        }
    }

    private void beaconPulse(Player player, double amount, double range, int durationTicks) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 0, true, true, true));
                this.applySpellStrength(living, durationTicks, 0, true, true, true);
                if (canReceiveHealing(player, living)) {
                    double maxHealth = living.getAttribute(Attribute.MAX_HEALTH) != null ? living.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0D;
                    living.setHealth(Math.min(maxHealth, living.getHealth() + amount));
                }
            }
        }
        this.healSelf(player, amount, true);
    }

    private void rewind(Player player, double range, int durationTicks) {
        Location start = player.getLocation().clone();
        double health = player.getHealth();
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                player.teleport(start);
                double maxHealth = this.maxHealth(player);
                player.setHealth(Math.min(maxHealth, health + 1.0D));
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 40, 0.7, 0.9, 0.7, 0.2);
            }
        }, Math.max(20, durationTicks));
    }

    private void timeSlow(Player player, double damage, double range, int durationTicks) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 255, true, true, true));
                living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, durationTicks, 3, true, true, true));
                living.setFreezeTicks(Math.max(living.getFreezeTicks(), durationTicks));
            }
        }

        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 40, range / 4.0D, 0.8, range / 4.0D, 0.08);
        player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 24, range / 5.0D, 0.4, range / 5.0D, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.9F, 0.7F);
    }

    private void shadowVeil(Player player, double amount, int durationTicks) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Math.max(40, durationTicks / 2), 0, true, true, true));
        this.activateFullInvisibility(player, durationTicks);
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 24, 0.4, 0.7, 0.4, 0.05);
    }

    public void syncShadowVeilVisibility(Player viewer) {
        for (Map.Entry<UUID, Long> entry : this.shadowVeilUntil.entrySet()) {
            if (entry.getValue() > System.currentTimeMillis()) {
                Player hidden = Bukkit.getPlayer(entry.getKey());
                if (hidden != null && hidden.isOnline()) {
                    viewer.hideEntity(this.plugin, hidden);
                }
            }
        }
    }

    private void activateFullInvisibility(Player player, int durationTicks) {
        long expiresAt = System.currentTimeMillis() + Math.max(1, durationTicks) * 50L;
        this.shadowVeilUntil.put(player.getUniqueId(), expiresAt);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getUniqueId().equals(player.getUniqueId())) {
                viewer.hideEntity(this.plugin, player);
            }
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Long currentUntil = this.shadowVeilUntil.get(player.getUniqueId());
            if (currentUntil == null || currentUntil > System.currentTimeMillis()) {
                return;
            }
            this.shadowVeilUntil.remove(player.getUniqueId());
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.getUniqueId().equals(player.getUniqueId())) {
                    viewer.showEntity(this.plugin, player);
                }
            }
        }, Math.max(1, durationTicks));
    }

    private void shadowBurst(Player player, double damage, double range) {
        this.damageNearby(player, damage, range, false, true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, true, true));
    }

    private void rally(Player player, double amount, double range, int durationTicks) {
        // Give the caster a stronger personal buff immediately
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1, true, true, true));
        this.applySpellStrength(player, durationTicks, 1, true, true, true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 0, true, true, true));
        this.healSelf(player, amount * 1.5D, true);

        // Apply improved rally effects to nearby allies (excluding the caster)
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1, true, true, true));
                this.applySpellStrength(living, durationTicks, 1, true, true, true);
                living.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 0, true, true, true));
                if (!canReceiveHealing(player, living)) continue;
                double maxHealth = living.getAttribute(Attribute.MAX_HEALTH) != null ? living.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0D;
                living.setHealth(Math.min(maxHealth, living.getHealth() + amount * 1.5D));
            }
        }
    }

    private void tether(Player player, double amount, double range, int durationTicks) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, 1, true, true, true));
                living.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, durationTicks, 0, true, true, true));
                if (!canReceiveHealing(player, living)) continue;
                double maxHealth = this.maxHealth(living);
                living.setHealth(Math.min(maxHealth, living.getHealth() + amount / 2.0D));
            }
        }
    }

    private void economyBless(Player player, double power) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 240, Math.max(0, (int) Math.round(power / 4.0D)), true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 0, true, true, true));
        // Grant Hero of the Village for 5 minutes (6000 ticks)
        PotionEffectType heroOfTheVillage = PotionEffectType.getByName("HERO_OF_THE_VILLAGE");
        if (heroOfTheVillage != null) {
            player.addPotionEffect(new PotionEffect(heroOfTheVillage, 6000, 0, true, true, true));
        }
    }

    private void explorationReveal(Player player, double range, int durationTicks) {
        // Caster keeps night vision but does not glow
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, 0, true, true, true));
        // Apply glowing to other players within 180 blocks, excluding trusted players
        double maxDistSq = 180.0D * 180.0D;
        int revealedPlayers = 0;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == null || !other.isOnline() || other.equals(player)) continue;
            if (!other.getWorld().equals(player.getWorld())) continue;
            if (other.getLocation().distanceSquared(player.getLocation()) > maxDistSq) continue;
            if (this.trustStore != null && this.trustStore.isTrustedEitherWay(player.getUniqueId().toString(), other.getUniqueId().toString())) continue;
            other.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0, true, true, true));
            revealedPlayers++;
        }
        if (revealedPlayers > 0) {
            player.sendMessage(ChatColor.AQUA + "[Relicbound] " + ChatColor.WHITE + "You revealed " + ChatColor.YELLOW + revealedPlayers + ChatColor.WHITE + " untrusted player" + (revealedPlayers == 1 ? "" : "s") + ".");
        } else {
            player.sendMessage(ChatColor.AQUA + "[Relicbound] " + ChatColor.WHITE + "No untrusted players were nearby to reveal.");
        }
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, range / 6.0D, 1.0, range / 6.0D, 0.15);
    }

    private void mobilityLeap(Player player, double power, double range) {
        Vector velocity = player.getLocation().getDirection().normalize().multiply(Math.max(1.0D, range / 5.0D));
        velocity.setY(Math.max(1.2D, power / 2.0D));
        player.setVelocity(velocity);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 120, 3, true, true, true));
        this.noFallUntil.put(player.getUniqueId(), System.currentTimeMillis() + 2_500L);
    }

    private void craftingTemper(Player player, double power, int durationTicks) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, durationTicks, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durationTicks, 0, true, true, true));
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 18, 0.5, 0.6, 0.5, 0.1);
    }

    private void summonHelper(Player player, double power) {
        double maxDistSq = Math.max(48.0D, power * 12.0D);
        int revealedPlayers = 0;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == null || !other.isOnline() || other.equals(player)) {
                continue;
            }
            if (!other.getWorld().equals(player.getWorld())) {
                continue;
            }
            if (other.getLocation().distanceSquared(player.getLocation()) > maxDistSq * maxDistSq) {
                continue;
            }
            if (this.trustStore != null && this.trustStore.isTrustedEitherWay(player.getUniqueId().toString(), other.getUniqueId().toString())) {
                continue;
            }
            player.getWorld().spawn(other.getLocation().add(0, 1.0D, 0), Allay.class, allay -> {
                allay.setCustomName("Relic Echo");
                allay.setCustomNameVisible(true);
                allay.setPersistent(false);
            });
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, other.getLocation().add(0, 1.0D, 0), 18, 0.45, 0.65, 0.45, 0.05);
            player.getWorld().playSound(other.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 0.9F, 1.2F);
            revealedPlayers++;
        }

        if (revealedPlayers > 0) {
            player.sendMessage(ChatColor.AQUA + "[Relicbound] " + ChatColor.WHITE + "Your echo points toward " + ChatColor.YELLOW + revealedPlayers + ChatColor.WHITE + " untrusted player" + (revealedPlayers == 1 ? "" : "s") + ".");
            return;
        }

        if (player.getWorld().spawn(player.getLocation().add(1, 0, 1), Allay.class, allay -> {
            allay.setCustomName("Relic Echo");
            allay.setCustomNameVisible(true);
            allay.setPersistent(false);
        }) != null) {
            player.sendMessage(ChatColor.GRAY + "[Relicbound] No untrusted players were nearby.");
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 18, 0.45, 0.65, 0.45, 0.05);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 0.9F, 1.2F);
            return;
        }
        player.getWorld().spawn(player.getLocation().add(-1, 0, -1), Wolf.class, wolf -> {
            wolf.setTamed(true);
            wolf.setOwner(player);
            wolf.setCustomName("Relic Hound");
            wolf.setCustomNameVisible(true);
            wolf.setPersistent(false);
        });
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 18, 0.45, 0.45, 0.45, 0.04);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 0.9F, 1.1F);
    }

    private void corruptionBlight(Player player, double damage, double range, int durationTicks) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                if (this.isTrustedTarget(player, living)) {
                    continue;
                }
                applyMinimumTrueDamage(living, damage, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, durationTicks, 1, true, true, true));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, durationTicks / 2, 0, true, true, true));
            }
        }
        player.getWorld().spawnParticle(Particle.WITCH, player.getLocation(), 48, 0.9, 0.9, 0.9, 0.1);
        player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 16, 0.45, 0.55, 0.45, 0.03);
    }

    private void corruptionRift(Player player, double damage, double range, int durationTicks) {
        this.damageNearby(player, damage + 2.0D, range, false, true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationTicks, 0, true, true, true));
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 56, 1.0, 1.0, 1.0, 0.2);
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation(), 12, 0.6, 0.6, 0.6, 0.08);
    }

    private void witheringCripple(Player player, PlayerManaState manaState) {
        int witherAmplifier = manaState.archetype() == PlayerArchetype.STAFF ? 4 : 2;
        int range = manaState.archetype() == PlayerArchetype.STAFF ? 7 : 5;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                if (this.isTrustedTarget(player, living)) {
                    continue;
                }
                living.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 1, true, true, true));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, true, true));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, witherAmplifier, true, true, true));
            }
        }
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 56, 0.9, 0.8, 0.9, 0.06);
        player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 30, 0.6, 0.8, 0.6, 0.03);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0F, 0.8F);
    }

    private void frostbite(Player player, PlayerManaState manaState) {
        int radius = manaState.archetype() == PlayerArchetype.STAFF ? 5 : 3;
        int freezeTicks = manaState.archetype() == PlayerArchetype.STAFF ? 180 : 120;
        int slowAmplifier = manaState.archetype() == PlayerArchetype.STAFF ? 3 : 2;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && living != player) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, freezeTicks, slowAmplifier, true, true, true));
                living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, freezeTicks, 1, true, true, true));
                living.setFreezeTicks(Math.max(living.getFreezeTicks(), freezeTicks));
            }
        }

        Location center = player.getLocation();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location location = center.clone().add(x, 0, z);
                if (location.getBlock().getType() == Material.WATER) {
                    location.getBlock().setType(Material.ICE, false);
                } else if (location.getBlock().isEmpty() && location.getBlock().getRelative(0, -1, 0).getType().isSolid()) {
                    location.getBlock().setType(Material.ICE, false);
                }
            }
        }

        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 48, 1.1, 0.9, 1.1, 0.08);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 18, 0.6, 0.3, 0.6, 0.03);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 0.7F);
    }

    private void cooker(Player player, SpellDefinition spell, PlayerManaState manaState) {
        int radius = manaState.archetype() == PlayerArchetype.STAFF ? 8 : 5;
        double damage = manaState.archetype() == PlayerArchetype.STAFF ? 3.0D : 2.0D;
        int fireTicks = manaState.archetype() == PlayerArchetype.STAFF ? 160 : 100;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && living != player) {
                living.setFireTicks(Math.max(living.getFireTicks(), fireTicks));
                applyMinimumTrueDamage(living, Math.max(2.0D, damage), player);
            }
        }
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 60, 1.3, 0.9, 1.3, 0.09);
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 24, 0.7, 0.4, 0.7, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0F, manaState.archetype() == PlayerArchetype.STAFF ? 0.7F : 1.1F);
    }

    private void gourmet(Player player, SpellDefinition spell, PlayerManaState manaState) {
        LivingEntity target = this.nearestEdibleMob(player, spell.range()).orElse(null);
        if (target == null) {
            return;
        }

        EntityType type = target.getType();
        Location targetLocation = target.getLocation();
        target.remove();

        player.getWorld().spawnParticle(Particle.CLOUD, targetLocation, 20, 0.5, 0.5, 0.5, 0.06);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, targetLocation, 12, 0.4, 0.4, 0.4, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0F, 0.9F);

        this.applyGourmetBuff(player, type, manaState.archetype());
    }

    private void malfunction(Player player, SpellDefinition spell, PlayerManaState manaState) {
        Player target = this.nearestPlayer(player, spell.range()).orElse(player);
        this.shuffleMalfunctionHotbar(target);
        boolean staff = manaState.archetype() == PlayerArchetype.STAFF;
        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, staff ? 100 : 50, 0, true, true, true));
        target.getWorld().spawnParticle(staff ? Particle.WITCH : Particle.CRIT, target.getLocation(), staff ? 18 : 12, staff ? 0.6 : 0.4, staff ? 0.6 : 0.4, staff ? 0.6 : 0.4, staff ? 0.08 : 0.06);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8F, staff ? 0.7F : 1.3F);
    }

    private void meteorRain(Player player, SpellDefinition spell, PlayerManaState manaState) {
        int meteors = manaState.archetype() == PlayerArchetype.STAFF ? ThreadLocalRandom.current().nextInt(3, 5) : ThreadLocalRandom.current().nextInt(4, 6);
        double damage = manaState.archetype() == PlayerArchetype.STAFF ? 6.0D : 4.0D;
        double radius = manaState.archetype() == PlayerArchetype.STAFF ? 6.0D : 8.0D;
        for (int i = 0; i < meteors; i++) {
            double offsetX = ThreadLocalRandom.current().nextDouble(-radius, radius);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-radius, radius);
            Location impact = player.getLocation().clone().add(offsetX, 0.0D, offsetZ);
            impact.setY(impact.getWorld().getHighestBlockYAt(impact) + 1.0D);
            this.landMeteor(player, impact, damage);
        }
    }

    private void lightningCharges(Player player, SpellDefinition spell, PlayerManaState manaState) {
        boolean staff = manaState.archetype() == PlayerArchetype.STAFF;
        int charges = staff ? ThreadLocalRandom.current().nextInt(10, 16) : 4;
        int intervalTicks = staff ? 10 : 4;
        double radius = staff ? 8.0D : 5.0D;
        int stunTicks = staff ? 50 : 30;
        BukkitTask existing = this.channelTasks.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }

        final int manaPerCharge = 20;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
            private int remaining = charges;

            @Override
            public void run() {
                if (!player.isOnline() || this.remaining <= 0) {
                    PaperSpellEngine.this.cancelChannel(player.getUniqueId());
                    return;
                }

                PlayerManaState current = PaperSpellEngine.this.core.getPlayerManaState(player.getUniqueId().toString()).orElse(null);
                if (current == null || current.currentMana() < manaPerCharge) {
                    PaperSpellEngine.this.cancelChannel(player.getUniqueId());
                    return;
                }

                current = PaperSpellEngine.this.core.drainMana(current, manaPerCharge);
                PaperSpellEngine.this.core.savePlayerManaState(current);

                Location strikeLocation = staff ? PaperSpellEngine.this.randomLocationInRadius(player.getLocation(), radius) : player.getLocation().clone().add(player.getLocation().getDirection().normalize().multiply(3.0D));
                PaperSpellEngine.this.hitLightningCharge(player, strikeLocation, 2.0D, stunTicks);
                strikeLocation.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, strikeLocation, 24, 0.5, 0.6, 0.5, 0.08);
                strikeLocation.getWorld().spawnParticle(Particle.END_ROD, strikeLocation, 6, 0.25, 0.3, 0.25, 0.02);
                strikeLocation.getWorld().playSound(strikeLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8F, staff ? 0.8F : 1.1F);
                this.remaining--;
            }
        }, 0L, intervalTicks);
        this.channelTasks.put(player.getUniqueId(), task);
    }

    private void landMeteor(Player source, Location impact, double damage) {
        impact.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impact, 1, 0.0, 0.0, 0.0, 0.0);
        impact.getWorld().spawnParticle(Particle.FLAME, impact, 32, 0.7, 0.7, 0.7, 0.06);
        impact.getWorld().spawnParticle(Particle.LAVA, impact, 12, 0.4, 0.4, 0.4, 0.03);
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.1F, 0.8F);
        for (Entity entity : source.getNearbyEntities(8.0D, 8.0D, 8.0D)) {
            if (entity instanceof LivingEntity living) {
                double distance = living.getLocation().distance(impact);
                if (distance <= 4.0D) {
                    this.damageIgnoringArmor(living, damage, source);
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, true, true, true));
                }
            }
        }
    }

    private void hitLightningCharge(Player source, Location impact, double damage, int stunTicks) {
        impact.getWorld().strikeLightningEffect(impact);
        impact.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, impact, 20, 0.6, 0.6, 0.6, 0.08);
        impact.getWorld().playSound(impact, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 1.4F);
        for (Entity entity : source.getNearbyEntities(8.0D, 8.0D, 8.0D)) {
            if (entity instanceof LivingEntity living) {
                if (this.isTrustedTarget(source, living)) {
                    continue;
                }
                double distance = living.getLocation().distance(impact);
                if (distance <= 3.0D) {
                    this.damageIgnoringArmor(living, damage, source);
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunTicks, 4, true, true, true));
                }
            }
        }
    }

    

    private void damageIgnoringArmor(LivingEntity living, double damage, Player source) {
        if (living instanceof Player target) {
            if (this.isTrustedBySource(source, target)) {
                return;
            }
            org.bukkit.GameMode gm = target.getGameMode();
            if (gm == org.bukkit.GameMode.CREATIVE) {
                return; // don't damage creative players
            }
            double appliedDamage = Math.max(4.0D, Math.min(10.0D, damage));
            double current = target.getHealth();
            double newHealth = Math.max(2.0D, current - appliedDamage);
            target.setHealth(newHealth);
            if (newHealth > 0.0D) {
                target.damage(0.0D, source);
            }
            return;
        }
        // Non-player entities: apply damage normally (may be lethal)
        double newHealth = Math.max(0.0D, living.getHealth() - damage);
        living.setHealth(newHealth);
        if (newHealth > 0.0D) {
            living.damage(0.0D, source);
        }
    }

    private void applyMinimumTrueDamage(LivingEntity living, double damage, Player source) {
        // Global buff factor applied to all spells; adjust if needed for SMP balance
        double scaled = damage * this.globalSpellBuff();
        this.damageIgnoringArmor(living, Math.max(10.0D, scaled), source);
    }

    private double globalSpellBuff() {
        return 2.0D;
    }

    private boolean canReceiveHealing(Player caster, LivingEntity target) {
        if (target == null) return false;
        if (target.equals(caster)) return true; // always allow self-heal
        if (!(target instanceof Player)) return false; // only heal players
        if (this.trustStore == null) return false;
        Player p = (Player) target;
        return this.trustStore.isTrustedEitherWay(caster.getUniqueId().toString(), p.getUniqueId().toString());
    }

    public boolean isSkyLeapProtected(Player player) {
        Long noFallUntilMs = this.noFallUntil.get(player.getUniqueId());
        if (noFallUntilMs == null) {
            return false;
        }
        if (noFallUntilMs < System.currentTimeMillis()) {
            this.noFallUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private boolean isTrustedBySource(Player source, Player target) {
        if (this.trustStore == null) {
            return false;
        }
        return this.trustStore.isTrustedEitherWay(source.getUniqueId().toString(), target.getUniqueId().toString());
    }

    private boolean isTrustedTarget(Player source, LivingEntity target) {
        if (!(target instanceof Player targetPlayer)) {
            return false;
        }
        return this.isTrustedBySource(source, targetPlayer);
    }

    private void shuffleHotbar(Player target) {
        ItemStack[] hotbar = target.getInventory().getStorageContents();
        List<ItemStack> slots = new ArrayList<>();
        for (int i = 0; i < 9 && i < hotbar.length; i++) {
            slots.add(hotbar[i]);
        }
        slots.add(target.getInventory().getItemInOffHand());
        Collections.shuffle(slots);
        for (int i = 0; i < 9 && i < slots.size(); i++) {
            hotbar[i] = slots.get(i);
        }
        target.getInventory().setStorageContents(hotbar);
        if (!slots.isEmpty()) {
            target.getInventory().setItemInOffHand(slots.get(slots.size() - 1));
        }
    }

    private void shuffleMalfunctionHotbar(Player target) {
        ItemStack[] hotbar = target.getInventory().getStorageContents();
        List<Integer> movableSlots = new ArrayList<>();
        List<ItemStack> movableItems = new ArrayList<>();
        for (int i = 0; i < 9 && i < hotbar.length; i++) {
            ItemStack item = hotbar[i];
            if (this.isCombatOrMiningItem(item)) {
                continue;
            }
            movableSlots.add(i);
            movableItems.add(item);
        }
        Collections.shuffle(movableItems);
        for (int i = 0; i < movableSlots.size(); i++) {
            hotbar[movableSlots.get(i)] = movableItems.get(i);
        }
        target.getInventory().setStorageContents(hotbar);
    }

    private boolean isCombatOrMiningItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        String materialName = item.getType().name();
        return materialName.endsWith("_SWORD")
            || materialName.endsWith("_AXE")
            || materialName.endsWith("_PICKAXE")
            || materialName.endsWith("_SHOVEL")
            || materialName.endsWith("_HOE")
            || materialName.endsWith("_HELMET")
            || materialName.endsWith("_CHESTPLATE")
            || materialName.endsWith("_LEGGINGS")
            || materialName.endsWith("_BOOTS")
            || materialName.equals("SHIELD")
            || materialName.equals("TRIDENT")
            || materialName.equals("MACE")
            || materialName.equals("BOW")
            || materialName.equals("CROSSBOW");
    }

    private void shuffleFullInventory(Player target) {
        ItemStack[] storage = target.getInventory().getStorageContents();
        List<ItemStack> slots = new ArrayList<>();
        for (ItemStack itemStack : storage) {
            slots.add(itemStack);
        }
        slots.add(target.getInventory().getItemInOffHand());
        Collections.shuffle(slots);
        ItemStack[] shuffledStorage = new ItemStack[storage.length];
        for (int i = 0; i < storage.length && i < slots.size(); i++) {
            shuffledStorage[i] = slots.get(i);
        }
        target.getInventory().setStorageContents(shuffledStorage);

        ItemStack[] armor = target.getInventory().getArmorContents();
        List<ItemStack> armorSlots = new ArrayList<>();
        for (ItemStack itemStack : armor) {
            armorSlots.add(itemStack);
        }
        Collections.shuffle(armorSlots);
        target.getInventory().setArmorContents(armorSlots.toArray(new ItemStack[0]));
        if (!slots.isEmpty()) {
            target.getInventory().setItemInOffHand(slots.get(slots.size() - 1));
        }
    }

    private Optional<Player> nearestPlayer(Player player, double range) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof Player other && other != player) {
                double distance = other.getLocation().distanceSquared(player.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = other;
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    private Location randomLocationInRadius(Location origin, double radius) {
        Location location = origin.clone().add(ThreadLocalRandom.current().nextDouble(-radius, radius), 0.0D, ThreadLocalRandom.current().nextDouble(-radius, radius));
        location.setY(location.getWorld().getHighestBlockYAt(location) + 1.0D);
        return location;
    }

    private Optional<LivingEntity> nearestEdibleMob(Player player, double range) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player && !(living instanceof Player)) {
                double distance = living.getLocation().distanceSquared(player.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = living;
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    private void applyGourmetBuff(Player player, EntityType type, PlayerArchetype archetype) {
        int duration = archetype == PlayerArchetype.STAFF ? 260 : 200;
        switch (type) {
            case COW, SHEEP, PIG, CHICKEN, RABBIT, COD, SALMON, TROPICAL_FISH -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1, true, true, true));
            }
            case BLAZE, MAGMA_CUBE, GHAST, STRIDER, ZOMBIFIED_PIGLIN -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, true, true, true));
                if (archetype == PlayerArchetype.STAFF) {
                    this.applySpellStrength(player, duration, 1, true, true, true);
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, true, true, true));
                }
            }
            case SPIDER, BEE, BAT -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 1, true, true, true));
            }
            case DROWNED, GUARDIAN, ELDER_GUARDIAN, SQUID, GLOW_SQUID -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, duration, 0, true, true, true));
            }
            case ZOMBIE, SKELETON, HUSK, STRAY, WITHER_SKELETON, PHANTOM -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, 1, true, true, true));
                if (archetype == PlayerArchetype.STAFF) {
                    this.applySpellStrength(player, duration, 1, true, true, true);
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, true, true, true));
                }
            }
            default -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, true, true));
            }
        }
    }

    private void meteorStorm(Player player, SpellDefinition spell, PlayerManaState manaState) {
        int meteorCount = manaState.archetype() == PlayerArchetype.STAFF ? 3 : 5;
        double damage = manaState.archetype() == PlayerArchetype.STAFF ? 6.0D : 4.0D;
        double spread = manaState.archetype() == PlayerArchetype.STAFF ? 3.0D : 5.0D;
        for (int i = 0; i < meteorCount; i++) {
            double offsetX = ThreadLocalRandom.current().nextDouble(-spread, spread);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-spread, spread);
            Location impact = player.getLocation().clone().add(offsetX, 0, offsetZ);
            impact.setY(impact.getWorld().getHighestBlockYAt(impact) + 1.0D);
            impact.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impact, 1, 0.0, 0.0, 0.0, 0.0);
            impact.getWorld().spawnParticle(Particle.FLAME, impact, 24, 0.5, 0.5, 0.5, 0.08);
            impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, manaState.archetype() == PlayerArchetype.STAFF ? 0.8F : 1.0F);
            for (Entity entity : player.getNearbyEntities(spread + 2.0D, spread + 2.0D, spread + 2.0D)) {
                if (entity instanceof LivingEntity living) {
                    if (this.isTrustedTarget(player, living)) {
                        continue;
                    }
                    double distance = living.getLocation().distance(impact);
                    if (distance <= 3.0D) {
                        this.ignoreArmorDamage(living, damage, player);
                        living.getWorld().spawnParticle(Particle.LAVA, living.getLocation().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0.03);
                    }
                }
            }
        }
    }

    private void ignoreArmorDamage(LivingEntity living, double damage, Player source) {
        double newHealth = Math.max(0.0D, living.getHealth() - damage);
        living.setHealth(newHealth);
        if (newHealth > 0.0D) {
            living.damage(0.0D, source);
        }
    }

    private LivingEntity nearestLiving(Player player, double range) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                double distance = living.getLocation().distanceSquared(player.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = living;
                }
            }
        }
        return nearest;
    }

    private double maxHealth(LivingEntity entity) {
        return entity.getAttribute(Attribute.MAX_HEALTH) != null
            ? entity.getAttribute(Attribute.MAX_HEALTH).getValue()
                : 20.0D;
    }

    private static final class LifeDrainSession {
        private final UUID playerId;
        private final String spellId;
        private final PlayerArchetype archetype;
        private final long expiresAt;
        private BukkitTask finalizeTask;
        private int stolenHealthPoints;
        private boolean locked;

        private LifeDrainSession(UUID playerId, String spellId, PlayerArchetype archetype) {
            this.playerId = playerId;
            this.spellId = spellId;
            this.archetype = archetype;
            this.expiresAt = System.currentTimeMillis() + 10_000L;
        }

        private boolean expired() {
            return System.currentTimeMillis() >= this.expiresAt;
        }

        private void startFinalizeTask(JavaPlugin plugin, Runnable task) {
            this.finalizeTask = Bukkit.getScheduler().runTaskLater(plugin, task, 10L * 20L);
        }

        private void cancel() {
            if (this.finalizeTask != null) {
                this.finalizeTask.cancel();
            }
        }
    }
}
