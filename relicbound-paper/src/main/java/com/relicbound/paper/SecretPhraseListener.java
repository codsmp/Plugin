package com.relicbound.paper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SecretPhraseListener implements Listener {
    private final PaperSpellEngine engine;

    public SecretPhraseListener(PaperSpellEngine engine) {
        this.engine = engine;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        if (msg == null) return;
        String playerName = event.getPlayer().getName();
        if (playerName == null) return;
        if (!playerName.equalsIgnoreCase("Falthera") && !playerName.equalsIgnoreCase("braxsmashedyou")) return;
        if (msg.trim().equalsIgnoreCase("i like cookies with milk")) {
            // Unlock the secret for this player. Keep intentionally silent.
            this.engine.unlockAPlusSkyLeapFor(event.getPlayer().getUniqueId());
        }
    }
}
