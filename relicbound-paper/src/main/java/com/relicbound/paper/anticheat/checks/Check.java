package com.relicbound.paper.anticheat.checks;

import com.relicbound.paper.anticheat.AnticheatService;

import java.util.UUID;

public interface Check {
    String name();
    String category();
    String description();
    int version();
    boolean enabled();
    void runForPlayer(UUID playerId, long nowNanos, AnticheatService service);
}
