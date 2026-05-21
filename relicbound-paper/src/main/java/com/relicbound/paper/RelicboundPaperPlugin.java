package com.relicbound.paper;

import com.relicbound.core.CoreContext;
import com.relicbound.core.RelicboundCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RelicboundPaperPlugin extends JavaPlugin {
    private RelicboundCore core;
    private PaperSpellEngine spellEngine;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        PaperPlatformAdapter adapter = new PaperPlatformAdapter(this);
        this.core = adapter.core();
        this.core.initialize(new CoreContext(
                adapter.capabilities(),
                adapter.progressionService(),
                adapter.relicCatalog(),
                adapter.spellCatalog(),
                adapter.relicProgressionService(),
                adapter.manaService(),
                adapter.playerRelicStateRepository(),
                adapter.playerManaStateRepository()
        ));
            this.spellEngine = new PaperSpellEngine(this, this.core);
        Bukkit.getPluginManager().registerEvents(new RelicJoinListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new RelicMenuListener(this, this.core), this);
            Bukkit.getPluginManager().registerEvents(new SpellMenuListener(this, this.core, this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new EssenceGainListener(this.core), this);
        Bukkit.getPluginManager().registerEvents(new ArchetypeSelectionListener(this.core), this);
            Bukkit.getPluginManager().registerEvents(new SpellCombatListener(this.spellEngine), this);

        // Start mana-related tasks
        new ManaBarDisplay(this, this.core).startDisplayTask();
        new ManaRegenTask(this, this.core).startRegenTask();

        if (this.getCommand("relicbound") != null) {
            this.getCommand("relicbound").setExecutor(new RelicboundCommand(this, this.core));
        }
    }

    @Override
    public void onDisable() {
        if (this.core != null) {
            this.core.shutdown();
        }
    }
}
