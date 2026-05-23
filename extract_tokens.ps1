Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = 'relicbound-paper/libs/paper-api-26.1.2.build.64-stable.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)

function ListTokens($entryName) {
    $entry = $zip.GetEntry($entryName)
    if (-not $entry) { Write-Output "$entryName: NOT FOUND"; return }
    $ms = New-Object System.IO.MemoryStream
    $entry.Open().CopyTo($ms)
    $bytes = $ms.ToArray()
    $s = [System.Text.Encoding]::ASCII.GetString($bytes)
    $matches = [regex]::Matches($s, '[A-Z0-9_]{3,}') | ForEach-Object { $_.Value }
    $uniq = $matches | Sort-Object -Unique
    Write-Output "--- $entryName tokens ---"
    $uniq | Select-Object -First 200
}

ListTokens 'org/bukkit/Particle.class'
ListTokens 'org/bukkit/potion/PotionEffectType.class'
ListTokens 'org/bukkit/attribute/Attribute.class'

$zip.Dispose()
