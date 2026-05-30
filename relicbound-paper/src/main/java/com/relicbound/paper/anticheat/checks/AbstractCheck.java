package com.relicbound.paper.anticheat.checks;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.config.CheckSettings;
import com.relicbound.paper.anticheat.config.AnticheatConfig;
import com.relicbound.paper.anticheat.violations.ViolationEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractCheck implements Check {
    protected final String name;
    protected final String category;
    protected final String description;
    protected final int version;

    protected AbstractCheck(String name, String category, String description, int version) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.version = version;
    }

    @Override
    public String name() { return this.name; }

    @Override
    public String category() { return this.category; }

    @Override
    public String description() { return this.description; }

    @Override
    public int version() { return this.version; }

    @Override
    public boolean enabled() { return true; }

    protected CheckSettings settings(AnticheatService service) {
        AnticheatConfig cfg = service.config();
        return cfg.check(this.name.toLowerCase(java.util.Locale.ROOT));
    }

    protected ViolationEntry addViolation(AnticheatService service, UUID playerId, double amount, String reason, Map<String, String> evidence) {
        return service.violations().add(playerId, this.name, amount, reason, System.nanoTime(), evidence == null ? new HashMap<>() : evidence);
    }
}
