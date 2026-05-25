package com.relicbound.core.model;

public enum RelicTier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5,
    ASCENSION_1,
    ASCENSION_2,
    ASCENSION_3,
    ASCENSION_4,
    ASCENSION_5;

    public boolean isAscension() {
        return switch (this) {
            case ASCENSION_1, ASCENSION_2, ASCENSION_3, ASCENSION_4, ASCENSION_5 -> true;
            default -> false;
        };
    }

    public static RelicTier maxAscension() {
        return ASCENSION_5;
    }
}
