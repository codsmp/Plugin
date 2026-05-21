package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.DefaultRelicboundCore;
import com.relicbound.core.catalog.DefaultRelicCatalog;
import com.relicbound.core.catalog.RelicCatalog;
import com.relicbound.core.catalog.DefaultSpellCatalog;
import com.relicbound.core.catalog.SpellCatalog;
import com.relicbound.core.PlayerRelicStateRepository;
import com.relicbound.core.PlayerManaStateRepository;
import com.relicbound.core.model.PlatformCapabilities;
import com.relicbound.core.platform.LoaderFamily;
import com.relicbound.core.platform.PlatformAdapter;
import com.relicbound.core.progression.DefaultRelicProgressionService;
import com.relicbound.core.progression.RelicProgressionService;
import com.relicbound.core.progression.ProgressionService;
import com.relicbound.core.progression.ManaService;
import com.relicbound.core.progression.DefaultManaService;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPlatformAdapter implements PlatformAdapter {
    private final JavaPlugin plugin;

    public PaperPlatformAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public RelicboundCore core() {
        return new DefaultRelicboundCore();
    }

    @Override
    public String platformId() {
        return "paper";
    }

    @Override
    public LoaderFamily loaderFamily() {
        return LoaderFamily.PLUGIN;
    }

    @Override
    public PlatformCapabilities capabilities() {
        return PlatformCapabilities.detectModernPaper();
    }

    @Override
    public ProgressionService progressionService() {
        return new InMemoryProgressionService();
    }

    @Override
    public RelicProgressionService relicProgressionService() {
        return new DefaultRelicProgressionService();
    }

    @Override
    public RelicCatalog relicCatalog() {
        return new DefaultRelicCatalog();
    }

    @Override
    public SpellCatalog spellCatalog() {
        return new DefaultSpellCatalog();
    }

    @Override
    public ManaService manaService() {
        return new DefaultManaService();
    }

    @Override
    public PlayerRelicStateRepository playerRelicStateRepository() {
        return new YamlPlayerRelicStateRepository(this.plugin);
    }

    @Override
    public PlayerManaStateRepository playerManaStateRepository() {
        return new YamlPlayerManaStateRepository(this.plugin);
    }
