package com.relicbound.paper;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class SpellMenuHolder implements InventoryHolder {
    private final String playerId;
    private final SpellMenuMode mode;
    private Inventory inventory;

    public SpellMenuHolder(String playerId, SpellMenuMode mode) {
        this.playerId = playerId;
        this.mode = mode;
    }

    public String playerId() {
        return this.playerId;
    }

    public SpellMenuMode mode() {
        return this.mode;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
