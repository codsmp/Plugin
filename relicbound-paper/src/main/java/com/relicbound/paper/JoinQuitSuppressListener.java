package com.relicbound.paper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinQuitSuppressListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            event.setJoinMessage(null);
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            event.setQuitMessage(null);
        } catch (Throwable ignored) {}
    }
}
