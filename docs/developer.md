# Developer Guide

How to add a new spell and regenerate the resource-pack.

1. Open `relicbound-core/src/main/java/.../catalog/DefaultSpellCatalog.java` and call `register(...)` with a new `SpellDefinition`.

2. Set appropriate `manaCost` and `manaPerSecond` values and `MaterialBinding` custom model data.

3. Rebuild the project:

```
./gradlew :relicbound-core:build :relicbound-paper:build
```

4. Regenerate the resource pack (PowerShell):

```
Set-Location 'c:\Users\Aaryadev\Desktop\Relicbound SMP'
.\tools\generate-resource-pack.ps1
```

5. Test in-game. If the spell needs engine logic (new `SpellEffectType`), add handling inside `PaperSpellEngine`.

Notes:
- The generator parses `DefaultSpellCatalog` registration lines; keep the registration format consistent.
- `resource-pack/manifest.json` is the authoritative list used by the docs generator.
