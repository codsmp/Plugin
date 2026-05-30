package com.relicbound.paper.anticheat.confidence;

import java.util.HashMap;
import java.util.Map;

public final class ConfidenceState {
    private double confidence;
    private long lastUpdateNanos;
    private final Map<String, Long> recentSignals = new HashMap<>();

    public double confidence() {
        return this.confidence;
    }

    public long lastUpdateNanos() {
        return this.lastUpdateNanos;
    }

    public void addSignal(String signal, double weight, long nowNanos) {
        long lastSignal = this.recentSignals.getOrDefault(signal, 0L);
        double multiplier = lastSignal == 0L || nowNanos - lastSignal > 8_000_000_000L ? 1.0D : 0.35D;
        this.confidence = Math.min(100.0D, this.confidence + (weight * multiplier));
        this.recentSignals.put(signal, nowNanos);
        this.lastUpdateNanos = nowNanos;
    }

    public void decay(long nowNanos, double ratePerSecond) {
        if (ratePerSecond <= 0.0D) {
            return;
        }
        if (this.lastUpdateNanos == 0L) {
            this.lastUpdateNanos = nowNanos;
            return;
        }
        long elapsed = Math.max(0L, nowNanos - this.lastUpdateNanos);
        if (elapsed == 0L) {
            return;
        }
        double seconds = elapsed / 1_000_000_000.0D;
        this.confidence = Math.max(0.0D, this.confidence - (seconds * ratePerSecond));
        this.lastUpdateNanos = nowNanos;
    }
}