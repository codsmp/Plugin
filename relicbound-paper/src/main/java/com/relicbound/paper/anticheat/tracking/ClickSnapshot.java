package com.relicbound.paper.anticheat.tracking;

public record ClickSnapshot(long nanoTime, String actionType, boolean attack) {
}