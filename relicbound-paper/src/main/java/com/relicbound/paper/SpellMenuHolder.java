package com.relicbound.paper;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class SpellMenuHolder implements InventoryHolder {
    private final String playerId;
    private Inventory inventory;

    public SpellMenuHolder(String playerId) {
        this.playerId = playerId;
    }

    public String playerId() {
        return this.playerId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
