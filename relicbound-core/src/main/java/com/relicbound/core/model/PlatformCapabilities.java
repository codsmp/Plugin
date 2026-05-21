package com.relicbound.core.model;

import java.util.EnumSet;
import java.util.Set;

public record PlatformCapabilities(Set<PlatformCapability> capabilities) {
    public static PlatformCapabilities detectModernPaper() {
        return new PlatformCapabilities(EnumSet.of(
                PlatformCapability.ITEM_COMPONENTS,
                PlatformCapability.CUSTOM_MODEL_DATA,
                PlatformCapability.ACTION_BAR,
                PlatformCapability.BOSS_BAR,
                PlatformCapability.RESOURCE_PACK_PROMPT,
                PlatformCapability.DATABASE_BACKGROUND_IO,
                PlatformCapability.CUSTOM_SOUND_EVENTS,
                PlatformCapability.CUSTOM_PARTICLES
        ));
    }

    public boolean supports(PlatformCapability capability) {
        return this.capabilities.contains(capability);
    }
}
