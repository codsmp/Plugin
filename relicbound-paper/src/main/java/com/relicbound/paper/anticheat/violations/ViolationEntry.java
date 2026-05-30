package com.relicbound.paper.anticheat.violations;

import java.util.Map;

public record ViolationEntry(
        String checkName,
        double added,
        double total,
        String reason,
        long nanoTime,
        Map<String, String> evidence
) {
}