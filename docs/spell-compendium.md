# Minecraft Cod SMP Full Spell Ability List

This reference reflects the current live spell engine and catalog. Where a spell has explicit wand/staff behavior, that difference is noted. Where the engine currently uses shared scaling instead of a separate branch, the spell is described by its actual runtime effect.

## Fire

### Ember Burst
School: Fire
Type: Offensive cone spell
Description: Ember Burst releases a cone of flame in front of the caster. Enemies caught inside take damage and ignite.
Wand Version: Smaller cone, lower damage, shorter burn.
Staff Version: Wider cone, higher damage, longer burn.

### Phoenix Dash
School: Fire
Type: Mobility spell
Description: Phoenix Dash launches the caster forward in a burst of fire energy for quick engages or escapes.
Wand Version: Shorter dash and lower mana pressure.
Staff Version: Longer dash with stronger fire-themed mobility.

## Water

### Tide Salve
School: Water
Type: Healing spell
Description: Tide Salve restores health, applies regeneration, removes weak burn effects, and clears short poison or wither pressure.
Wand Version: Faster, lighter heal.
Staff Version: Stronger healing and broader support impact.

### Undertow Wave
School: Water
Type: Crowd control spell
Description: Undertow Wave releases a heavy surge that knocks enemies backward and creates space.
Wand Version: Smaller push zone.
Staff Version: Stronger knockback and larger area.

## Storm

### Thunder Lance
School: Storm
Type: Targeted strike spell
Description: Thunder Lance calls lightning onto the nearest target and can chain into nearby enemies.
Wand Version: Lower burst and shorter chain pressure.
Staff Version: Higher damage with more chain potential.

### Tempest Chain
School: Storm
Type: Chain lightning spell
Description: Tempest Chain jumps storm energy between grouped enemies.
Wand Version: Fewer jumps.
Staff Version: More jumps, larger reach, and stronger damage.

### Lightning Charges
School: Storm
Type: Storm barrage spell
Description: Lightning Charges creates a sequence of rapid lightning hits. Wand use fires a short burst of fixed strikes; staff use turns it into a roaming storm of random strikes.
Wand Version: 4 rapid blasts, 20 mana each, 1 heart damage, about 1.5 second stun.
Staff Version: 10 to 15 random strikes, 1 heart damage each, longer stun, more battlefield control.

## Void

### Gravity Snare
School: Void
Type: Channelled crowd control spell
Description: Gravity Snare creates a void field that pulls nearby enemies inward while the spell remains active.
Wand Version: Smaller pull field and lighter mana pressure.
Staff Version: Stronger pull, larger radius, heavier sustain cost.

### Rift Step
School: Void
Type: Teleportation spell
Description: Rift Step tears open space and moves the caster to a nearby location.
Wand Version: Shorter teleport range.
Staff Version: Longer repositioning distance.

## Light

### Dawn Aegis
School: Light
Type: Defensive shield spell
Description: Dawn Aegis surrounds the caster in radiant protection and sustains a defensive buff over time.
Wand Version: Smaller sustain footprint.
Staff Version: Stronger protection and larger aura support.

### Sanctify
School: Light
Type: Purification spell
Description: Sanctify clears harmful darkness effects and restores nearby allies through holy energy.
Wand Version: Faster cast and lighter healing.
Staff Version: Wider cleanse radius and stronger healing.

## Nature

### Root Bind
School: Nature
Type: Immobilization spell
Description: Root Bind traps enemies in magical roots and, in the current engine, applies extreme Slowness so targets cannot move.
Wand Version: Smaller bind area.
Staff Version: Stronger root pressure and wider area.

### Bloom Mend
School: Nature
Type: Healing spell
Description: Bloom Mend restores health with regenerative nature energy.
Wand Version: Faster activation with lighter healing.
Staff Version: Greater healing and broader ally coverage.

## Stone

### Seismic Line
School: Stone
Type: Ground shock spell
Description: Seismic Line releases a crushing shockburst around the caster that knocks enemies back and deals damage.
Wand Version: Smaller impact zone.
Staff Version: Larger impact zone and heavier push.

### Bulwark Wall
School: Stone
Type: Defensive barrier spell
Description: Bulwark Wall reinforces the caster with resistance and triggers a stone-flavored knockback burst. The current engine emphasizes defensive control more than a literal placed wall.
Wand Version: Lower sustain pressure.
Staff Version: Stronger defensive control and greater mana drain.

## Celestial

### Starfall
School: Celestial
Type: Burst celestial spell
Description: Starfall smites the nearest target with heavenly lightning and glowing impact.
Wand Version: Lower damage, faster cooldown.
Staff Version: Higher damage and a stronger impact zone.

### Astral Beacon
School: Celestial
Type: Support utility spell
Description: Astral Beacon surrounds nearby allies with speed, strength, and healing while also restoring the caster.
Wand Version: Lower sustain drain and smaller field.
Staff Version: Larger support field and stronger utility.

### Meteor Surge
School: Celestial
Type: Burst meteor spell
Description: Meteor Surge calls down multiple meteors in a concentrated blast.
Wand Version: More meteors, broader spread, lighter impact.
Staff Version: Fewer meteors, tighter spread, stronger hits.

### Meteor Rain
School: Celestial
Type: Area artillery spell
Description: Meteor Rain summons several meteors from the sky, each dealing armor-ignoring impact damage in a wide area.
Wand Version: More meteors with broader coverage.
Staff Version: Fewer meteors, but stronger impacts and a tighter strike pattern.

## Time

### Rewind Step
School: Time
Type: Temporal recovery spell
Description: Rewind Step snaps the caster back toward a safer previous position after a short delay and restores a bit of health.
Wand Version: Shorter rewind range.
Staff Version: Stronger recovery and repositioning value.

### Slow Field
School: Time
Type: Area debuff spell
Description: Slow Field heavily reduces enemy movement and combat speed in the target area while applying pressure over time.
Wand Version: Smaller area.
Staff Version: Larger distortion zone and stronger crowd control.

## Shadow

### Veil Strike
School: Shadow
Type: Burst ambush spell
Description: Veil Strike lashes nearby enemies with shadow energy and blinds them after the hit.

### Umbra Walk
School: Shadow
Type: Stealth spell
Description: Umbra Walk cloaks the caster in darkness, adding invisibility, speed, and darkened visual pressure for escape or repositioning.

## Support

### Rally Chant
School: Support
Type: Buff aura spell
Description: Rally Chant now gives the caster and nearby trusted allies speed, strength, resistance, and immediate healing. It is much stronger than the original version and is meant for team fights.
Wand Version: Faster support setup with lower sustain cost.
Staff Version: Stronger buffs and bigger teamfight value.

### Life Tether
School: Support
Type: Healing link spell
Description: Life Tether shares healing with nearby allies through a continuous support effect.
Wand Version: Lower sustain cost.
Staff Version: Stronger healing share and broader support potential.

## Economy

### Coin Blessing
School: Economy
Type: Utility spell
Description: Coin Blessing boosts luck and economic outcomes. It also now grants Hero of the Village for 5 minutes.

## Exploration

### Trail Reveal
School: Exploration
Type: Tracking spell
Description: Trail Reveal gives the caster Night Vision and highlights other players within 180 blocks with Glowing, except trusted players.

## Mobility

### Sky Leap
School: Mobility
Type: Movement spell
Description: Sky Leap launches the caster upward and forward with much stronger propulsion than the original baseline.

## Crafting

### Temper Touch
School: Crafting
Type: Enhancement spell
Description: Temper Touch improves tools or equipment for a limited duration and adds crafting speed support.

## Summoner

### Echo Call
School: Summoner
Type: Summon spell
Description: Echo Call summons a spectral helper. The engine prefers an Allay-style helper and falls back to a wolf ally if needed.

## Corruption

### Blight Wave
School: Corruption
Type: Decay spell
Description: Blight Wave spreads poison and wither pressure outward in a damaging burst.

### Abyss Rift
School: Corruption
Type: Void destruction spell
Description: Abyss Rift tears open a dangerous breach of corruption and void energy.

### Withering Cripple
School: Corruption
Type: Channelled curse spell
Description: Withering Cripple continuously applies Darkness II, Slowness II, and Wither. The curse stays active until the caster stops channeling or runs out of mana.
Wand Version: 10 mana drained per second and weaker wither pressure.
Staff Version: 20 mana drained per second with full Wither V-style pressure and wider area.

### Lifedrain
School: Corruption
Type: Combat sustain spell
Description: After activation, critical hits siphon hearts from enemies. Up to 3 hearts can be stolen, and the stolen health persists for a short duration before fading.

## Utility

### Malfunction
School: Utility
Type: Inventory disruption spell
Description: Malfunction scrambles a target's inventory to create confusion and mistakes.
Wand Version: Shuffles the hotbar and offhand.
Staff Version: Shuffles the full inventory and offhand.

## Elemental

### Frostbite
School: Elemental
Type: Freeze spell
Description: Frostbite freezes enemies and converts nearby water and terrain into ice.

### Cooker
School: Elemental
Type: Burning area spell
Description: Cooker ignites enemies in a nearby radius and applies continuous fire pressure.

### Gourmet
School: Elemental
Type: Consumption buff spell
Description: Gourmet consumes a nearby mob and grants a temporary buff based on the mob type consumed.
