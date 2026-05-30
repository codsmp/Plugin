package com.relicbound.paper.anticheat.violations;

import com.relicbound.paper.anticheat.config.AnticheatConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ViolationEngine {
    private final Map<UUID, ViolationState> states = new ConcurrentHashMap<>();

    public ViolationState state(UUID playerId) {
        return this.states.computeIfAbsent(playerId, key -> new ViolationState());
    }

    public ViolationEntry add(UUID playerId, String checkName, double amount, String reason, long nanoTime, Map<String, String> evidence) {
        return this.state(playerId).check(checkName).add(checkName, amount, reason, nanoTime, evidence);
    }

    public void decayAll(AnticheatConfig config, long nowNanos) {
        for (ViolationState state : this.states.values()) {
            state.decay(this.defaultDecay(config), nowNanos);
        }
    }

    public double totalVl(UUID playerId) {
        return this.state(playerId).totalVl();
    }

    public double checkVl(UUID playerId, String checkName) {
        return this.state(playerId).check(checkName).vl();
    }

    private double defaultDecay(AnticheatConfig config) {
        return Math.max(0.02D, config.general().lagCompensationFactor() * 0.10D);
    }
}