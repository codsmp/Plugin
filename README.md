# Relicbound SMP

Relicbound SMP is a Minecraft RPG-SMP framework built around relic progression, spell casting, essence growth, and platform adapters.

## Modules

- `relicbound-core`: version-agnostic gameplay, relic, spell, and progression logic
- `relicbound-paper`: Paper/Purpur adapter layer and the first playable implementation

## Current features

- Deterministic starter relic assignment
- Persistent player relic state storage
- Tier-based relic progression
- Large cross-referenced spell catalog with unlocks and cooldown-based casting
- Paper spellbook and relic menus
- In-game guide menu for onboarding, controls, and progression tips
- Basic essence gain from combat and mining
- GitHub release workflow for tagged builds
- Resource pack zip built from vanilla Minecraft textures

## Model support

Spell items use custom model data and vanilla Minecraft item textures, so an existing public server can ship the pack without maintaining custom PNG art.

## Public release

Tagged GitHub releases publish both the plugin jar and the resource pack zip. The Paper plugin can optionally push the resource pack to players on join through `relicbound-paper/src/main/resources/config.yml`, and players can open `/relicbound guide` or the Guide item in the relic menu for a quick walkthrough.

The plugin jar is compiled for Java 21, so the server that loads it needs Java 21 or newer.

## Current scope

The implementation is functional as a Paper-first gameplay slice. Future loaders still need their own adapters and the broader boss, quest, event, and world systems are not complete yet.

## Documentation & Branding

Documentation has been added using MkDocs with the Material theme. See the `docs/` folder for source content and branding assets (logo and favicon).

A GitHub Action (`.github/workflows/docs.yml`) will build and deploy the docs site to GitHub Pages on push to `main`/`master`.

## Recent changes

- Performance: reduced join/quit spikes by making player state saves asynchronous, deferring resource-pack and visibility/team sync work off the immediate join tick, and added lightweight join timing logs for diagnostics.
- Teams: scoreboard team handling updated to reliably restore team prefixes after logout/login and to reduce scoreboard churn on player syncs.
- Cleanup: a server-wide dropped-item cleanup task now removes loose item entities every 60 seconds to reduce entity-related lag.
- Security: removed secret-phrase and hidden progression backdoors.


