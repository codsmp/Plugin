package com.relicbound.paper.anticheat.violations;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ViolationState {
    private final Map<String, CheckViolationState> byCheck = new LinkedHashMap<>();

    public CheckViolationState check(String checkName) {
        return this.byCheck.computeIfAbsent(checkName, key -> new CheckViolationState());
    }

    public Map<String, CheckViolationState> all() {
        return this.byCheck;
    }

    public double totalVl() {
        double total = 0.0D;
        for (CheckViolationState state : this.byCheck.values()) {
            total += state.vl();
        }
        return total;
    }

    public void decay(double decayPerSecond, long nowNanos) {
        for (CheckViolationState state : this.byCheck.values()) {
            state.decay(decayPerSecond, nowNanos);
        }
    }

    public static final class CheckViolationState {
        private double vl;
        private long lastUpdateNanos;
        private long lastFailNanos;
        private String lastReason = "";
        private final Deque<ViolationEntry> recent = new ArrayDeque<>();

        public double vl() {
            return this.vl;
        }

        public long lastUpdateNanos() {
            return this.lastUpdateNanos;
        }

        public long lastFailNanos() {
            return this.lastFailNanos;
        }

        public String lastReason() {
            return this.lastReason;
        }

        public Deque<ViolationEntry> recent() {
            return this.recent;
        }

        public ViolationEntry add(String checkName, double amount, String reason, long nanoTime, Map<String, String> evidence) {
            this.decay(0.0D, nanoTime);
            this.vl += amount;
            this.lastUpdateNanos = nanoTime;
            this.lastFailNanos = nanoTime;
            this.lastReason = reason;
            ViolationEntry entry = new ViolationEntry(checkName, amount, this.vl, reason, nanoTime, evidence);
            this.recent.addFirst(entry);
            while (this.recent.size() > 8) {
                this.recent.removeLast();
            }
            return entry;
        }

        public void decay(double decayPerSecond, long nowNanos) {
            if (decayPerSecond <= 0.0D) {
                this.lastUpdateNanos = nowNanos;
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
            this.vl = Math.max(0.0D, this.vl - (seconds * decayPerSecond));
            this.lastUpdateNanos = nowNanos;
        }
    }
}