package com.relicbound.paper;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RelicMenuHolder implements InventoryHolder {
    private final String playerId;
    private Inventory inventory;

    public RelicMenuHolder(String playerId) {
        this.playerId = playerId;
    }

    public String playerId() {
        return this.playerId;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
