package com.relicbound.paper.anticheat.listener;

import com.relicbound.paper.anticheat.AnticheatService;
import com.relicbound.paper.anticheat.tracking.ClickSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ClickListener implements Listener {
    private final AnticheatService service;

    public ClickListener(AnticheatService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!this.service.enabled()) return;
        String action = event.getAction().name();
        boolean attack = action.contains("LEFT_CLICK");
        ClickSnapshot snap = new ClickSnapshot(System.nanoTime(), action, attack);
        this.service.registry().getOrCreate(event.getPlayer()).recordClick(snap);
    }
}