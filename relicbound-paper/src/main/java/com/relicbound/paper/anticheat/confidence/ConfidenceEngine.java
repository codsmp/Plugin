package com.relicbound.paper.anticheat.confidence;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfidenceEngine {
    private final Map<UUID, ConfidenceState> states = new ConcurrentHashMap<>();

    public ConfidenceState state(UUID playerId) {
        return this.states.computeIfAbsent(playerId, key -> new ConfidenceState());
    }

    public void addSignal(UUID playerId, String signal, double weight, long nowNanos) {
        this.state(playerId).addSignal(signal, weight, nowNanos);
    }

    public double confidence(UUID playerId) {
        return this.state(playerId).confidence();
    }

    public void decayAll(long nowNanos, double ratePerSecond) {
        for (ConfidenceState state : this.states.values()) {
            state.decay(nowNanos, ratePerSecond);
        }
    }
}