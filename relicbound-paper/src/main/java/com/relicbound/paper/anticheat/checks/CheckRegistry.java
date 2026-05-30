package com.relicbound.paper.anticheat.checks;

import java.util.ArrayList;
import java.util.List;

public final class CheckRegistry {
    private final List<Check> checks = new ArrayList<>();

    public void register(Check check) {
        this.checks.add(check);
    }

    public List<Check> all() {
        return List.copyOf(this.checks);
    }
}
