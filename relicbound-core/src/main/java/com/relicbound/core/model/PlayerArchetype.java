package com.relicbound.core.model;

public enum PlayerArchetype {
    WAND("Fast spellcaster", 1.15, 0.85, 0.80),
    STAFF("Heavy spellcaster", 0.70, 1.35, 1.60);

    private final String displayName;
    private final double castSpeedMultiplier;
    private final double damageMultiplier;
    private final double manaDrainMultiplier;

    PlayerArchetype(String displayName, double castSpeedMultiplier, double damageMultiplier, double manaDrainMultiplier) {
        this.displayName = displayName;
        this.castSpeedMultiplier = castSpeedMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.manaDrainMultiplier = manaDrainMultiplier;
    }

    public String displayName() {
        return this.displayName;
    }

    public double castSpeedMultiplier() {
        return this.castSpeedMultiplier;
    }

    public double damageMultiplier() {
        return this.damageMultiplier;
    }

    public double manaDrainMultiplier() {
        return this.manaDrainMultiplier;
    }
}
