package com.relicbound.paper.anticheat.logging;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public final class AnticheatLogger {
    private final JavaPlugin plugin;
    private final File dir;

    public AnticheatLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "anticheat/logs");
        this.dir.mkdirs();
    }

    public void log(String filename, String line) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            File f = new File(this.dir, filename);
            try (FileWriter w = new FileWriter(f, true)) {
                w.write(Instant.now().toString() + " " + line + "\n");
            } catch (IOException e) {
                this.plugin.getLogger().warning("Failed to write anticheat log: " + e.getMessage());
            }
        });
    }
}
