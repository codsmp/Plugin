# Quickstart

This guide helps server admins install the Witch Plugin and resource-pack.

1. Build the plugin with Gradle:

```
./gradlew :relicbound-paper:build
```

2. Drop the generated JAR into your Paper server `plugins/` folder.

3. Install the resource-pack generated in `resource-pack/` or host it and set `server.properties` `resource-pack` URL.

To regenerate the resource pack locally:

Windows PowerShell (from repository root):

```
Set-Location 'c:\Users\Aaryadev\Desktop\Relicbound SMP'
.\tools\generate-resource-pack.ps1
```

4. Start the server and test: join the server, choose an archetype (WAND or STAFF), and open the spell menu.
