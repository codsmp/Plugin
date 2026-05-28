# Witch Plugin Resource Pack

This folder is generated to provide spell item models for Witch Plugin.

## What it contains

- `assets/minecraft/models/item/paper.json`: custom model data overrides for spell items
- `assets/minecraft/models/item/stick.json`: custom model data override for the starter wand
- `assets/relicbound/models/item/spell/*.json`: spell item models that point to vanilla Minecraft textures
- `assets/relicbound/models/item/wand.json`: the quick wand model
- `assets/relicbound/textures/item/texture.png`: the quick wand texture

## Notes

The current spell UI uses `PAPER` as the base item so the resource pack only needs one override chain. Each spell model reuses a built-in Minecraft item texture, so there are no custom PNGs to maintain.
