package com.relicbound.paper.anticheat.tracking;

public record MovementSnapshot(
        long nanoTime,
        long tick,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        double deltaX,
        double deltaY,
        double deltaZ,
        boolean onGround,
        boolean sprinting,
        boolean sneaking,
        boolean inVehicle,
        EnvironmentFlags environment
) {
}