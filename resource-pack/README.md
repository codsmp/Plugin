# Relicbound SMP Resource Pack

This folder is generated to provide spell item models for Relicbound SMP.

## What it contains

- `assets/minecraft/models/item/paper.json`: custom model data overrides for spell items
- `assets/relicbound/models/item/spell/*.json`: spell item models that point to vanilla Minecraft textures

## Notes

The current spell UI uses `PAPER` as the base item so the resource pack only needs one override chain. Each spell model reuses a built-in Minecraft item texture, so there are no custom PNGs to maintain.
