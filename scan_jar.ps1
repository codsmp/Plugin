Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = 'relicbound-paper/libs/paper-api-26.1.2.build.64-stable.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)

function CheckEntry($entryName, $names) {
    $entry = $zip.GetEntry($entryName)
    if (-not $entry) { Write-Output "$entryName: NOT FOUND"; return }
    $ms = New-Object System.IO.MemoryStream
    $entry.Open().CopyTo($ms)
    $bytes = $ms.ToArray()
    $s = [System.Text.Encoding]::ASCII.GetString($bytes)
    foreach ($n in $names) {
        $found = $s.Contains($n)
        Write-Output "$entryName -> $n : $(if ($found) { 'FOUND' } else { 'MISSING' })"
    }
}

$particleNames = @('SMOKE_NORMAL','SMOKE_LARGE','SPELL_WITCH','VILLAGER_HAPPY','PORTAL','SNOWFLAKE','CRIT_MAGIC','EXPLOSION_LARGE','BLOCK_CRACK','BLOCK_DUST','ELECTRIC_SPARK','FLAME','CLOUD','EXPLOSION_HUGE','EXPLOSION_LARGE','SNOWBALL','SNOWFLAKE')
$potionNames = @('SLOW','SPEED','INVISIBILITY','DARKNESS','BLINDNESS','REGENERATION','ABSORPTION','LUCK','SATURATION','NIGHT_VISION','JUMP','FAST_DIGGING','INCREASE_DAMAGE','POISON','WITHER','CONFUSION','SLOW_DIGGING')
$attributeNames = @('GENERIC_MAX_HEALTH','GENERIC_ATTACK_DAMAGE','GENERIC_FOLLOW_RANGE')

CheckEntry 'org/bukkit/Particle.class' $particleNames
CheckEntry 'org/bukkit/PotionEffectType.class' $potionNames
CheckEntry 'org/bukkit/attribute/Attribute.class' $attributeNames

$zip.Dispose()
