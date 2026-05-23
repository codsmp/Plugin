$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$coreCatalog = Join-Path $repoRoot 'relicbound-core\src\main\java\com\relicbound\core\catalog\DefaultSpellCatalog.java'
$packRoot = Join-Path $repoRoot 'resource-pack'
$paperModelsDir = Join-Path $packRoot 'assets\minecraft\models\item'
$spellModelsDir = Join-Path $packRoot 'assets\relicbound\models\item\spell'
$spellTexturesDir = Join-Path $packRoot 'assets\relicbound\textures\item\spell'
$wandModelDir = Join-Path $packRoot 'assets\relicbound\models\item'
$wandTextureDir = Join-Path $packRoot 'assets\relicbound\textures\item'

New-Item -ItemType Directory -Force -Path $paperModelsDir | Out-Null
New-Item -ItemType Directory -Force -Path $spellModelsDir | Out-Null
New-Item -ItemType Directory -Force -Path $wandModelDir | Out-Null
New-Item -ItemType Directory -Force -Path $wandTextureDir | Out-Null

if (Test-Path $spellTexturesDir) {
    Remove-Item -Recurse -Force $spellTexturesDir
}

$spellLines = Get-Content $coreCatalog | Where-Object { $_ -match 'register\(new SpellDefinition\(' }
$spells = foreach ($line in $spellLines) {
    if ($line -match 'new SpellDefinition\("(?<id>[^"]+)",\s*"(?<name>[^"]+)",\s*"(?<model>[^"]+)",\s*binding\("(?<material>[^"]+)",\s*(?<cmd>\d+)\),\s*RelicTier\.(?<tier>[A-Z0-9_]+),\s*SpellEffectType\.(?<effect>[A-Z_]+).*?,\s*"(?<desc>[^"]*)",\s*(?<manaCost>\d+),\s*(?<manaPerSecond>\d+)\)\);') {
        [pscustomobject]@{
            Id = $Matches.id
            Name = $Matches.name
            ModelKey = $Matches.model
            Material = $Matches.material
            CustomModelData = [int]$Matches.cmd
            Tier = $Matches.tier
            Effect = $Matches.effect
            Description = $Matches.desc
            ManaCost = [int]$Matches.manaCost
            ManaPerSecond = [int]$Matches.manaPerSecond
        }
    }
}

if (-not $spells) {
    throw 'No spells were found in DefaultSpellCatalog.java.'
}

$rootTexture = Join-Path $repoRoot 'texture.png'
$rootWandModel = Join-Path $repoRoot 'wand.json'
if (Test-Path $rootTexture) {
    Copy-Item $rootTexture (Join-Path $wandTextureDir 'texture.png') -Force
}
if (Test-Path $rootWandModel) {
    Copy-Item $rootWandModel (Join-Path $wandModelDir 'wand.json') -Force
}

$paperOverrides = @()
foreach ($spell in $spells) {
    $paperOverrides += [ordered]@{
        predicate = [ordered]@{ custom_model_data = $spell.CustomModelData }
        model = "relicbound:item/spell/$($spell.Id)"
    }
}

$paperModel = [ordered]@{
    parent = 'minecraft:item/generated'
    textures = [ordered]@{ layer0 = 'minecraft:item/paper' }
    overrides = $paperOverrides
}
($paperModel | ConvertTo-Json -Depth 8) | Set-Content (Join-Path $paperModelsDir 'paper.json') -Encoding UTF8

foreach ($spell in $spells) {
    $vanillaTexture = "minecraft:item/$($spell.Material.ToLowerInvariant())"
    $spellModel = [ordered]@{
        parent = 'minecraft:item/generated'
        textures = [ordered]@{ layer0 = $vanillaTexture }
    }
    ($spellModel | ConvertTo-Json -Depth 6) | Set-Content (Join-Path $spellModelsDir "$($spell.Id).json") -Encoding UTF8
}

$manifest = [ordered]@{
    generatedAt = (Get-Date).ToString('s')
    spellCount = $spells.Count
    entries = $spells | Select-Object Id, Name, ModelKey, Material, CustomModelData, Tier, Effect, Description, ManaCost, ManaPerSecond
}
($manifest | ConvertTo-Json -Depth 8) | Set-Content (Join-Path $packRoot 'manifest.json') -Encoding UTF8
