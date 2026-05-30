package com.relicbound.paper.anticheat.tracking;

public record VelocitySnapshot(long nanoTime, long tick, double x, double y, double z, String reason) {
}