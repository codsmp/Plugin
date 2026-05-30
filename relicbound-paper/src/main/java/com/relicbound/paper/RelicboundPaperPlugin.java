package com.relicbound.paper;

import com.relicbound.core.CoreContext;
import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Map;
import java.util.stream.Collectors;

public final class RelicboundPaperPlugin extends JavaPlugin {
    private PaperPlatformAdapter adapter;
    private RelicboundCore core;
    private PaperSpellEngine spellEngine;
    private PlayerTrustStore trustStore;
    private PlayerTeamStore teamStore;
    private GracePeriodController gracePeriodController;
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
        this.teamStore = new PlayerTeamStore(this);
        this.spellEngine = new PaperSpellEngine(this, this.core, this.trustStore);
        // Load any persisted Bliss return locations from previous sessions
        this.spellEngine.loadBlissReturns();
        this.gracePeriodController = new GracePeriodController(this);
        Bukkit.getPluginManager().registerEvents(new RelicJoinListener(this, this.core, this.spellEngine, this.teamStore), this);
        Bukkit.getPluginManager().registerEvents(new RelicMenuListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new SpellMenuListener(this, this.core, this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new EssenceGainListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new ArchetypeSelectionListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new SpellCombatListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new PvpRulesListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TotemUnobtainableListener(), this);
        Bukkit.getPluginManager().registerEvents(new StarterItemProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new SkyLeapProtectionListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new GuideMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpellWandListener(this.core, this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new RootBindDisableListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new TrustDamageListener(this.trustStore, this.teamStore), this);
        Bukkit.getPluginManager().registerEvents(new DragonEggUnlockListener(this, this.core), this);
        Bukkit.getPluginManager().registerEvents(new BlissLifecycleListener(this, this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new InvisibleKillerDeathListener(this.spellEngine), this);
        Bukkit.getPluginManager().registerEvents(new CombatLogListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.gracePeriodController, this);
        Bukkit.getPluginManager().registerEvents(new TabIsolationListener(), this);
        Bukkit.getPluginManager().registerEvents(new RandomSpawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinQuitSuppressListener(), this);
        Bukkit.getPluginManager().registerEvents(new OneDayBanListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerVisibilityEnforcer(this, this.teamStore, this.trustStore), this);

        // Start mana-related tasks
        new ManaBarDisplay(this, this.core, this.spellEngine).startDisplayTask();
        new ManaRegenTask(this, this.core).startRegenTask();

        if (this.getCommand("info") != null) {
            RelicboundCommand executor = new RelicboundCommand(this, this.core, this.spellEngine);
            this.getCommand("info").setExecutor(executor);
            if (this.getCommand("spell") != null) this.getCommand("spell").setExecutor(executor);
            if (this.getCommand("witch") != null) this.getCommand("witch").setExecutor(executor);
            if (this.getCommand("witchspells") != null) this.getCommand("witchspells").setExecutor(executor);
            if (this.getCommand("witchupgrade") != null) this.getCommand("witchupgrade").setExecutor(executor);
            if (this.getCommand("witchgrant") != null) this.getCommand("witchgrant").setExecutor(executor);
            if (this.getCommand("witchreset") != null) this.getCommand("witchreset").setExecutor(new RelicResetCommand(this, this.adapter, this.trustStore));
        }

        // enchantlimit command removed

        if (this.getCommand("relicreset") != null) {
            this.getCommand("relicreset").setExecutor(new RelicResetCommand(this, this.adapter, this.trustStore));
        }

        if (this.getCommand("trust") != null) {
            this.getCommand("trust").setExecutor(new TrustCommand(this, this.trustStore));
        }

        this.teamStore.syncAllOnlinePlayers();

        if (this.getCommand("graceperiod") != null) {
            this.getCommand("graceperiod").setExecutor(this.gracePeriodController);
        }

        // Disable server advancement announcements so players won't see advancement messages in chat.
        try {
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            }
            // Also enforce for newly loaded worlds
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onWorldLoad(WorldLoadEvent evt) {
                    try {
                        evt.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                    } catch (Throwable ignored) {}
                }
            }, this);
        } catch (Throwable t) {
            this.getLogger().warning("Could not set announceAdvancements gamerule: " + t.getMessage());
        }

        this.runStartupChecks();
    }

    @Override
    public void onDisable() {
        if (this.core != null) {
            this.core.shutdown();
        }
        if (this.spellEngine != null) {
            this.spellEngine.saveBlissReturns();
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
            player.kickPlayer(org.bukkit.ChatColor.RED + "Witch data has been reset. Please reconnect.");
        }

        this.getLogger().warning("Witch data was reset by " + actorName);
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
