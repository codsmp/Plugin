package com.relicbound.paper.anticheat.tracking;

public record EnvironmentFlags(
        boolean inLiquid,
        boolean inWater,
        boolean inLava,
        boolean onHoney,
        boolean onSlime,
        boolean inCobweb,
        boolean onIce,
        boolean inPowderSnow,
        boolean onLadder,
        boolean onVine,
        boolean onGroundLike
) {
}