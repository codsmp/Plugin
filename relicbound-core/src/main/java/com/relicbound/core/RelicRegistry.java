package com.relicbound.core;

import com.relicbound.core.model.RelicDefinition;

import java.util.Optional;

public interface RelicRegistry {
    Optional<RelicDefinition> findById(String relicId);
}
