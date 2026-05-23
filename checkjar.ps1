Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('relicbound-paper/libs/paper-api-26.1.2.build.64-stable.jar')
$zip.Entries | Where-Object { $_.FullName -like 'org/bukkit/Particle*' -or $_.FullName -like 'org/bukkit/PotionEffectType*' -or $_.FullName -like 'org/bukkit/attribute/Attribute*' } | ForEach-Object { $_.FullName }
