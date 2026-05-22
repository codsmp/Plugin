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
            RelicboundCommand executor = new RelicboundCommand(this, this.core);
            this.getCommand("relicbound").setExecutor(executor);
            if (this.getCommand("relicboundspells") != null) this.getCommand("relicboundspells").setExecutor(executor);
            if (this.getCommand("relicboundupgrade") != null) this.getCommand("relicboundupgrade").setExecutor(executor);
            if (this.getCommand("relicboundgrant") != null) this.getCommand("relicboundgrant").setExecutor(executor);
            if (this.getCommand("rb") != null) this.getCommand("rb").setExecutor(executor);
        }
    }

    @Override
    public void onDisable() {
        if (this.core != null) {
            this.core.shutdown();
        }
    }
}
