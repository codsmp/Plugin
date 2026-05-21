# Architecture Overview

Relicbound is split into two main modules:

- `relicbound-core`: platform-agnostic logic, models and services (spell catalog, mana service, repositories).
- `relicbound-paper`: Paper (Minecraft) adapter, event listeners, and `PaperSpellEngine` which executes spells.

Key concepts:

- `CoreContext`: central record that wires services and repositories into `DefaultRelicboundCore`.
- `PlayerArchetype`: `WAND` and `STAFF` archetypes with distinct multipliers for cast speed, damage and mana drain.
- `ManaService` / `PlayerManaStateRepository`: manage player mana states and persist to YAML (`data/mana/{uuid}.yml`).
- `SpellDefinition` and `DefaultSpellCatalog`: spells are registered centrally with `manaCost` and `manaPerSecond` metadata used by the engine and the resource-pack generator.
- `PaperSpellEngine`: casting, channeling, cooldowns, and archetype scaling happen here.

Resource-pack generation: `tools/generate-resource-pack.ps1` reads `DefaultSpellCatalog` registrations and writes `resource-pack/manifest.json` and model overrides under `resource-pack/assets/relicbound`.
