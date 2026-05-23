package com.relicbound.paper;

import com.relicbound.core.CoreContext;
import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.stream.Collectors;

public final class RelicboundPaperPlugin extends JavaPlugin {
    private PaperPlatformAdapter adapter;
    private RelicboundCore core;
    private PaperSpellEngine spellEngine;
    private PlayerTrustStore trustStore;
    private volatile boolean resetInProgress;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.adapter = new PaperPlatformAdapter(this);
        this.core = this.adapter.core();
        this.core.initialize(new CoreContext(
            this.adapter.capabilities(),
            this.adapter.progressionService(),
            this.adapter.relicCatalog(),
            this.adapter.spellCatalog(),
            this.adapter.relicProgressionService(),
            this.adapter.manaService(),
            this.adapter.playerRelicStateRepository(),
            this.adapter.playerManaStateRepository()
        ));
        this.trustStore = new PlayerTrustStore(this);
        this.spellEngine = new PaperSpellEngine(this, this.core, this.trustStore);
        Bukkit.getPluginManager().registerEvents(new RelicJoinListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new RelicMenuListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new SpellMenuListener(this, this.core, this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new EssenceGainListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new ArchetypeSelectionListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new SpellCombatListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new PvpRulesListener(this), this);
        Bukkit.getPluginManager().registerEvents(new StarterItemProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new SkyLeapProtectionListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new SecretPhraseListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new GuideMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpellWandListener(this.core, this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new TrustDamageListener(this.trustStore), this);

        // Start mana-related tasks
        new ManaBarDisplay(this, this.core, this.spellEngine).startDisplayTask();
        new ManaRegenTask(this, this.core).startRegenTask();

        if (this.getCommand("relicbound") != null) {
            RelicboundCommand executor = new RelicboundCommand(this, this.core, this.spellEngine);
            this.getCommand("relicbound").setExecutor(executor);
            if (this.getCommand("relicboundspells") != null) this.getCommand("relicboundspells").setExecutor(executor);
            if (this.getCommand("relicboundupgrade") != null) this.getCommand("relicboundupgrade").setExecutor(executor);
            if (this.getCommand("relicboundgrant") != null) this.getCommand("relicboundgrant").setExecutor(executor);
            if (this.getCommand("rb") != null) this.getCommand("rb").setExecutor(executor);
        }

        // enchantlimit command removed

        if (this.getCommand("relicreset") != null) {
            this.getCommand("relicreset").setExecutor(new RelicResetCommand(this, this.adapter, this.trustStore));
        }

        if (this.getCommand("trust") != null) {
            this.getCommand("trust").setExecutor(new TrustCommand(this, this.trustStore));
        }

        this.runStartupChecks();
    }

    @Override
    public void onDisable() {
        if (this.core != null) {
            this.core.shutdown();
        }
    }

    public boolean isResetInProgress() {
        return this.resetInProgress;
    }

    public void executeFullReset(String actorName) {
        this.resetInProgress = true;
        this.adapter.resetPersistentState();
        this.trustStore.clear();

        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            player.kickPlayer(org.bukkit.ChatColor.RED + "Relicbound data has been reset. Please reconnect.");
        }

        this.getLogger().warning("Relicbound data was reset by " + actorName);
        org.bukkit.Bukkit.getScheduler().runTaskLater(this, () -> this.resetInProgress = false, 40L);
    }

    private void runStartupChecks() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String name = plugin.getName();
            String normalized = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
            if (normalized.contains("enchlimiter") || normalized.contains("enchantlimiter") || normalized.contains("ench limiter")) {
                this.getLogger().warning("Possible external enchant-limiter plugin detected: " + name + ". If enchants are still being removed, disable or remove that plugin and restart the server.");
            }
        }

        Map<String, Long> spellCounts = this.core.allSpells().stream()
            .collect(Collectors.groupingBy(SpellDefinition::id, Collectors.counting()));
        spellCounts.forEach((spellId, count) -> {
            if (count > 1) {
                this.getLogger().warning("Duplicate spell id detected: " + spellId + " (x" + count + ")");
            }
        });

        if (this.core.findSpell("dawn_aegis").isEmpty()) {
            this.getLogger().warning("Expected spell 'dawn_aegis' was not found in spell catalog.");
        }

        String packUrl = this.getConfig().getString("resource-pack.url", "");
        if (packUrl == null || packUrl.isBlank()) {
            this.getLogger().warning("resource-pack.url is empty; players will not receive an automatic pack prompt.");
        }
    }
}
