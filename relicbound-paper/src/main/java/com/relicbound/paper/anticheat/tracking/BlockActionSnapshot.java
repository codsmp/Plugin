package com.relicbound.paper.anticheat.tracking;

import org.bukkit.Material;

public record BlockActionSnapshot(long nanoTime, long tick, String action, Material material, double x, double y, double z) {
}