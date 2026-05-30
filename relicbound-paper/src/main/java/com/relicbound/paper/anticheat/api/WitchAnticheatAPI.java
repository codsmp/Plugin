package com.relicbound.paper.anticheat.api;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.violations.ViolationEntry;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WitchAnticheatAPI {
    private final AnticheatService service;

    public WitchAnticheatAPI(AnticheatService service) {
        this.service = service;
    }

    public double getTotalVl(UUID playerId) {
        return this.service.violations().totalVl(playerId);
    }

    public double getConfidence(UUID playerId) {
        return this.service.confidence().confidence(playerId);
    }

    public Map<String, ?> getSnapshot(UUID playerId) {
        var tracker = playerId == null ? null : this.service.registry().get(playerId);
        return Map.of(
                "vl", this.getTotalVl(playerId),
                "confidence", this.getConfidence(playerId),
                "movements", tracker == null ? 0 : tracker.movements().size(),
                "clicks", tracker == null ? 0 : tracker.clicks().size(),
                "combat", tracker == null ? 0 : tracker.combat().size()
        );
    }

    public void registerCheck(com.relicbound.paper.anticheat.checks.Check check) {
        this.service.config(); // noop ensure loaded
        // Expose registry via reflection-like access: this is intentionally minimal to avoid breaking API.
        try {
            java.lang.reflect.Field f = this.service.getClass().getDeclaredField("checkRegistry");
            f.setAccessible(true);
            Object registry = f.get(this.service);
            java.lang.reflect.Method m = registry.getClass().getMethod("register", com.relicbound.paper.anticheat.checks.Check.class);
            m.invoke(registry, check);
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to register check", t);
        }
    }
}
