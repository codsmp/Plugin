package com.relicbound.paper;

import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class ArchetypeSelectionHolder implements InventoryHolder {
    private final String playerId;

    public ArchetypeSelectionHolder(String playerId) {
        this.playerId = playerId;
    }

    public String playerId() {
        return this.playerId;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        return null;
    }
}
