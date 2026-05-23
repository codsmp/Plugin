Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('.tmp\relicbound-paper-0.1.0-SNAPSHOT.jar')
$zip.Entries | Where-Object { $_.FullName -eq 'plugin.yml' -or $_.FullName -eq 'com/relicbound/paper/RelicboundPaperPlugin.class' -or $_.FullName -like 'META-INF/*' } | ForEach-Object { $_.FullName }
$zip.Dispose()
